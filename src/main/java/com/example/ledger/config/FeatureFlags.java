package com.example.ledger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlags {

    @Value("${feature.create-transaction.enabled:true}")
    private boolean createTransactionEnabled;

    public boolean isCreateTransactionEnabled() {
        return createTransactionEnabled;
    }

    @Value("${feature.get-all-transactions.enabled:true}")
    private boolean getAllTransactionsEnabled;

    public boolean isGetAllTransactionsEnabled() {
        return getAllTransactionsEnabled;
    }
}
