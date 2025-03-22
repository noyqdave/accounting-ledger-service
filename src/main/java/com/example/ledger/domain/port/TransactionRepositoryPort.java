package com.example.ledger.domain.port;

import com.example.ledger.domain.model.Transaction;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);
}
