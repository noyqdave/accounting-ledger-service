package com.example.ledger.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FeatureFlagFilter extends OncePerRequestFilter {

    private final FeatureFlags featureFlags;

    public FeatureFlagFilter(FeatureFlags featureFlags) {
        this.featureFlags = featureFlags;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        if (shouldDisableFeature(request)) {
            sendFeatureDisabledResponse(response);
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean shouldDisableFeature(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // Map requests to feature flags
        if ("/transactions".equals(requestURI) && "POST".equals(method)) {
            return !featureFlags.isCreateTransactionEnabled();
        }
        
        if ("/transactions".equals(requestURI) && "GET".equals(method)) {
            return !featureFlags.isGetAllTransactionsEnabled();
        }
        
        return false;
    }

    private void sendFeatureDisabledResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Feature is disabled\"}");
    }
}
