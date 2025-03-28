package com.example.ledger.application.usecase;

import com.example.ledger.config.FeatureFlags;
import com.example.ledger.domain.model.Transaction;
import com.example.ledger.domain.port.TransactionRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllTransactionsService implements GetAllTransactionsUseCase {

    private final TransactionRepositoryPort repository;
    private final FeatureFlags featureFlags;

    public GetAllTransactionsService(TransactionRepositoryPort repository, FeatureFlags featureFlags) {
        this.repository = repository;
        this.featureFlags = featureFlags;
    }

    @Override
    public List<Transaction> getAll() {
        if (!featureFlags.isGetAllTransactionsEnabled()) {
            throw new IllegalStateException("Fetching all transactions is currently disabled");
        }
        return repository.findAll();
    }
}
