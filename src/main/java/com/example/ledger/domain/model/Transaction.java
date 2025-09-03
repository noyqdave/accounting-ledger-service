package com.example.ledger.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "A financial transaction in the ledger")
public class Transaction {
    
    @Schema(description = "Unique identifier for the transaction", example = "123e4567-e89b-12d3-a456-426614174000")
    private final UUID id;
    
    @Schema(description = "Transaction creation timestamp", example = "2024-01-15T10:30:00")
    private final LocalDateTime date;
    
    @Schema(description = "Transaction amount", example = "25.50")
    private final BigDecimal amount;
    
    @Schema(description = "Transaction description", example = "Office supplies")
    private final String description;
    
    @Schema(description = "Transaction type", example = "EXPENSE", allowableValues = {"EXPENSE", "REVENUE"})
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
