package com.example.ledger.adapters.in.web.dto;

import com.example.ledger.adapters.in.web.CreateTransactionRequest;
import com.example.ledger.application.usecase.CreateTransactionUseCase;
import com.example.ledger.domain.model.Transaction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody CreateTransactionRequest request) {
        // Convert DTO to domain model
        Transaction transaction = new Transaction(
                request.getAmount(),
                request.getDescription(),
                request.getType()
        );

        Transaction savedTransaction = createTransactionUseCase.create(transaction);
        return ResponseEntity.ok(savedTransaction);
    }
}

