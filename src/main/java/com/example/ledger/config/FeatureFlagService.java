package com.example.ledger.config;

/**
 * Service for checking feature flag status.
 * This interface allows for easy testing by mocking the implementation.
 */
public interface FeatureFlagService {
    /**
     * Checks if a feature is enabled.
     *
     * @param featureName the name of the feature flag
     * @return true if the feature is enabled, false otherwise
     * @throws IllegalArgumentException if the feature name is unknown
     */
    boolean isEnabled(String featureName);

    /**
     * Throws FeatureFlagDisabledException if the feature is disabled.
     *
     * @param featureName the name of the feature flag
     * @throws FeatureFlagDisabledException if the feature is disabled
     * @throws IllegalArgumentException if the feature name is unknown
     */
    void requireEnabled(String featureName);
}

