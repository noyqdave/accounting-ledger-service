package com.example.ledger.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {
    private final UUID id;
    private final LocalDateTime date;
    private final BigDecimal amount;
    private final String description;
    private final TransactionType type;

    public Transaction(BigDecimal amount, String description, TransactionType type) {
        this.id = UUID.randomUUID();
        this.date = LocalDateTime.now();
        this.amount = amount;
        this.description = description;
        this.type = type;
    }

    // Getters
}
