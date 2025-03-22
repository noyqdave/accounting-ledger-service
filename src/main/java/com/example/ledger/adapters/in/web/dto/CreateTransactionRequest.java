package com.example.ledger.adapters.in.web.dto;

import com.example.ledger.domain.model.TransactionType;

import java.math.BigDecimal;

public class CreateTransactionRequest {
    private BigDecimal amount;
    private String description;
    private TransactionType type;

    // Getters and setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}
