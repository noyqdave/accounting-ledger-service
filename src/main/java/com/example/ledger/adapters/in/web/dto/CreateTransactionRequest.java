package com.example.ledger.adapters.in.web.dto;

import com.example.ledger.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Request object for creating a new transaction")
public class CreateTransactionRequest {
    
    @Schema(description = "Transaction amount (must be positive)", example = "25.50", required = true)
    private BigDecimal amount;
    
    @Schema(description = "Transaction description", example = "Office supplies", required = true)
    private String description;
    
    @Schema(description = "Transaction type", example = "EXPENSE", allowableValues = {"EXPENSE", "REVENUE"}, required = true)
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
