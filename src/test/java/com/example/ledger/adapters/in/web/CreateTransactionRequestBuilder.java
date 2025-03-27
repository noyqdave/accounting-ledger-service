package com.example.ledger.adapters.in.web;

import com.example.ledger.adapters.in.web.dto.CreateTransactionRequest;
import com.example.ledger.domain.model.TransactionType;

import java.math.BigDecimal;

public class CreateTransactionRequestBuilder {
    private final CreateTransactionRequest request = new CreateTransactionRequest();

    public CreateTransactionRequestBuilder withAmount(BigDecimal amount) {
        request.setAmount(amount);
        return this;
    }

    public CreateTransactionRequestBuilder withDescription(String description) {
        request.setDescription(description);
        return this;
    }

    public CreateTransactionRequestBuilder withType(TransactionType type) {
        request.setType(type);
        return this;
    }

    public CreateTransactionRequest build() {
        return request;
    }
}
