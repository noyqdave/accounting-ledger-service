package com.example.ledger.application.usecase;

import com.example.ledger.domain.model.Transaction;
import com.example.ledger.domain.port.TransactionRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllTransactionsService implements GetAllTransactionsUseCase {

    private final TransactionRepositoryPort repository;

    public GetAllTransactionsService(TransactionRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<Transaction> getAll() {

        return repository.findAll();
    }
}
