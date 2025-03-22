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

    public Transaction(UUID id, LocalDateTime date, BigDecimal amount, String description, TransactionType type) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.type = type;
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public TransactionType getType() {
        return type;
    }
}
