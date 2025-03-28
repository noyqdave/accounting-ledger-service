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
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description must not be null or empty");
        }


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
