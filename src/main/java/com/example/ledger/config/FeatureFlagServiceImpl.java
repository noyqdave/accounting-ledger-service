package com.example.ledger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Default implementation of FeatureFlagService that reads from application properties.
 */
@Service
public class FeatureFlagServiceImpl implements FeatureFlagService {

    @Value("${feature.create-transaction.enabled:true}")
    private boolean createTransactionEnabled;

    @Value("${feature.get-all-transactions.enabled:true}")
    private boolean getAllTransactionsEnabled;

    @Override
    public boolean isEnabled(String featureName) {
        return switch (featureName) {
            case "create-transaction" -> createTransactionEnabled;
            case "get-all-transactions" -> getAllTransactionsEnabled;
            default -> throw new IllegalArgumentException("Unknown feature flag: " + featureName);
        };
    }

    @Override
    public void requireEnabled(String featureName) {
        if (!isEnabled(featureName)) {
            throw new FeatureFlagDisabledException("Feature '" + featureName + "' is disabled");
        }
    }
}

