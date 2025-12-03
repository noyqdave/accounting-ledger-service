package com.example.ledger.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter that checks feature flags for specific endpoints.
 * This approach is cleaner than AOP and easier to test.
 */
@Component
public class FeatureFlagFilter extends OncePerRequestFilter {

    private final FeatureFlagService featureFlagService;
    private final ObjectMapper objectMapper;

    // Map of endpoint patterns to feature flag names
    private static final Map<String, String> ENDPOINT_FEATURE_MAP = Map.of(
            "POST /transactions", "create-transaction",
            "GET /transactions", "get-all-transactions"
    );

    public FeatureFlagFilter(FeatureFlagService featureFlagService, ObjectMapper objectMapper) {
        this.featureFlagService = featureFlagService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                   @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String endpointKey = method + " " + path;

        // Check if this endpoint has a feature flag requirement
        String featureName = ENDPOINT_FEATURE_MAP.get(endpointKey);
        
        if (featureName != null) {
            try {
                featureFlagService.requireEnabled(featureName);
            } catch (FeatureFlagDisabledException e) {
                handleFeatureDisabled(response, e);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleFeatureDisabled(HttpServletResponse response, FeatureFlagDisabledException e) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Feature is disabled");
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}

