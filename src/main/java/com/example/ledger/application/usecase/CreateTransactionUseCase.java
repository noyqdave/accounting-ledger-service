package com.example.ledger.application.usecase;

import com.example.ledger.domain.model.Transaction;

public interface CreateTransactionUseCase {
    Transaction create(Transaction transaction);
}
