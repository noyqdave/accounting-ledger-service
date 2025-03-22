package com.example.ledger.application.usecase;

import com.example.ledger.domain.model.Transaction;

import java.util.List;

public interface GetAllTransactionsUseCase {
    List<Transaction> getAll();
}
