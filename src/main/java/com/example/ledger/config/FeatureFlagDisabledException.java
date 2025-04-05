package com.example.ledger.config;

public class FeatureFlagDisabledException extends RuntimeException {
    public FeatureFlagDisabledException(String message) {
        super(message);
    }
}
