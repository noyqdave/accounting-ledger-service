package com.example.ledger.application.usecase;

import com.example.ledger.domain.model.Transaction;
import com.example.ledger.domain.port.TransactionRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class CreateTransactionService implements CreateTransactionUseCase {

    private final TransactionRepositoryPort repository;

    public CreateTransactionService(TransactionRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Transaction create(Transaction transaction) {
        return repository.save(transaction);
    }
}
