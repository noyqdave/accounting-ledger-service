package com.example.ledger.adapters.in.web;

import com.example.ledger.adapters.in.web.dto.CreateTransactionRequest;
import com.example.ledger.application.usecase.CreateTransactionUseCase;
import com.example.ledger.application.usecase.GetAllTransactionsUseCase;
import com.example.ledger.config.FeatureEnabled;
import com.example.ledger.domain.model.Transaction;
import com.example.ledger.config.FeatureFlags;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final GetAllTransactionsUseCase getAllTransactionsUseCase;
    private final FeatureFlags featureFlags;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase,
                                 GetAllTransactionsUseCase getAllTransactionsUseCase,
                                 FeatureFlags featureFlags) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.getAllTransactionsUseCase = getAllTransactionsUseCase;
        this.featureFlags = featureFlags;
    }

    @FeatureEnabled("create-transaction")
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

    @FeatureEnabled("get-all-transactions")
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {

        List<Transaction> transactions = getAllTransactionsUseCase.getAll();
        return ResponseEntity.ok(transactions);
    }
}

