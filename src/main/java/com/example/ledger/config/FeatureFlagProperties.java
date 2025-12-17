package com.example.ledger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for feature flag endpoints.
 * Maps HTTP method + path to feature names from application.yml.
 * 
 * Example YAML:
 * feature:
 *   endpoints:
 *     "POST /transactions": "create-transaction"
 *     "GET /transactions": "get-all-transactions"
 */
@Component
@ConfigurationProperties(prefix = "feature")
public class FeatureFlagProperties {

    private Map<String, String> endpoints = new HashMap<>();

    public Map<String, String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<String, String> endpoints) {
        this.endpoints = endpoints != null ? endpoints : new HashMap<>();
    }
}
