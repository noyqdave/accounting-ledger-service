package com.example.ledger.adapters.in.web;

import com.example.ledger.application.port.IdempotencyRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Inbound adapter that handles idempotency key processing for POST /transactions requests.
 * 
 * This filter intercepts HTTP requests and implements idempotency behavior:
 * - If Idempotency-Key header is present:
 *   - Check if key exists with same request hash → return cached response
 *   - Check if key exists with different request hash → return 409 Conflict
 *   - If key doesn't exist → continue processing, cache response after
 * - If Idempotency-Key header is not present → process normally
 * 
 * This is an inbound adapter because it processes incoming HTTP requests before they reach
 * the controller, similar to how TransactionController is an inbound adapter.
 */
@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String POST_TRANSACTIONS_PATH = "/transactions";

    private final IdempotencyRepositoryPort idempotencyRepository;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public IdempotencyFilter(IdempotencyRepositoryPort idempotencyRepository, 
                           ObjectMapper objectMapper,
                           MeterRegistry meterRegistry) {
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                   @NonNull HttpServletResponse response,
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Only process POST /transactions requests
        if (!"POST".equals(request.getMethod()) || !POST_TRANSACTIONS_PATH.equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        
        // If no idempotency key, process normally
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Wrap request to allow reading body multiple times
        CachedBodyHttpServletRequest requestWrapper = new CachedBodyHttpServletRequest(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // Validate idempotency key format
        if (!idempotencyRepository.isValidKey(idempotencyKey)) {
            handleInvalidKey(response);
            return;
        }

        // Track idempotency request metric
        meterRegistry.counter("idempotency.requests.total").increment();

        // Read request body to compute hash - this will cache it in the wrapper
        // We need to read it through the wrapper's input stream to cache it
        String requestBody = getRequestBody(requestWrapper);
        String requestHash = hashRequestBody(requestBody);

        // Check for cached response (same key, same request)
        Optional<IdempotencyRepositoryPort.IdempotencyResponse> cachedResponse = 
                idempotencyRepository.getCachedResponse(idempotencyKey, requestHash);
        
        if (cachedResponse.isPresent()) {
            // Track cache hit metric
            meterRegistry.counter("idempotency.cache.hits").increment();
            
            // Return cached response
            IdempotencyRepositoryPort.IdempotencyResponse cached = cachedResponse.get();
            response.setStatus(cached.getStatusCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(cached.getResponseBody());
            return;
        }

        // Check for conflict (same key, different request)
        if (idempotencyRepository.hasKeyWithDifferentHash(idempotencyKey, requestHash)) {
            handleConflict(response);
            return;
        }

        try {
            // Process request with wrapped request/response
            filterChain.doFilter(requestWrapper, responseWrapper);
            
            // Cache successful responses (status 200)
            if (responseWrapper.getStatus() == HttpStatus.OK.value()) {
                String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                IdempotencyRepositoryPort.IdempotencyResponse idempotencyResponse = 
                        new IdempotencyRepositoryPort.IdempotencyResponse(
                                responseWrapper.getStatus(), 
                                responseBody);
                idempotencyRepository.storeResponse(idempotencyKey, requestHash, idempotencyResponse);
            }
        } finally {
            // Copy cached response to actual response
            responseWrapper.copyBodyToResponse();
        }
    }

    private String getRequestBody(CachedBodyHttpServletRequest request) throws IOException {
        return request.getCachedBody();
    }
    
    /**
     * Custom HttpServletRequestWrapper that caches the request body
     * to allow multiple reads via getInputStream().
     */
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private byte[] cachedBody;
        
        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            // Cache the body immediately
            this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
        }
        
        public String getCachedBody() {
            return new String(cachedBody, StandardCharsets.UTF_8);
        }
        
        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new CachedBodyServletInputStream(cachedBody);
        }
        
        @Override
        public java.io.BufferedReader getReader() throws IOException {
            return new java.io.BufferedReader(
                new java.io.InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
        
        private static class CachedBodyServletInputStream extends ServletInputStream {
            private final byte[] cachedBody;
            private int lastIndexRetrieved = -1;
            private ReadListener readListener = null;
            
            public CachedBodyServletInputStream(byte[] cachedBody) {
                this.cachedBody = cachedBody;
            }
            
            @Override
            public boolean isFinished() {
                return lastIndexRetrieved == cachedBody.length - 1;
            }
            
            @Override
            public boolean isReady() {
                return true;
            }
            
            @Override
            public void setReadListener(ReadListener readListener) {
                this.readListener = readListener;
                if (!isFinished()) {
                    try {
                        readListener.onDataAvailable();
                    } catch (IOException e) {
                        readListener.onError(e);
                    }
                } else {
                    try {
                        readListener.onAllDataRead();
                    } catch (IOException e) {
                        readListener.onError(e);
                    }
                }
            }
            
            @Override
            public int read() throws IOException {
                int i;
                if (!isFinished()) {
                    i = cachedBody[lastIndexRetrieved + 1];
                    lastIndexRetrieved++;
                    if (isFinished() && readListener != null) {
                        try {
                            readListener.onAllDataRead();
                        } catch (IOException e) {
                            readListener.onError(e);
                            throw e;
                        }
                    }
                    return i;
                } else {
                    return -1;
                }
            }
        }
    }

    private String hashRequestBody(String requestBody) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(requestBody.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to compute request hash", e);
            // Fallback to simple hash code
            return String.valueOf(requestBody.hashCode());
        }
    }

    private void handleInvalidKey(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid idempotency key format");
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private void handleConflict(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.CONFLICT.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Idempotency key already used with different request parameters");
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
