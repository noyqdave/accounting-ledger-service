package com.example.ledger.adapters.in.web;

import com.example.ledger.adapters.in.web.dto.CreateTransactionRequest;
import com.example.ledger.application.usecase.CreateTransactionUseCase;
import com.example.ledger.application.usecase.GetAllTransactionsUseCase;
import com.example.ledger.config.TrackMetric;
import com.example.ledger.config.FeatureEnabled;
import com.example.ledger.domain.model.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transaction Management", description = "APIs for managing financial transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final GetAllTransactionsUseCase getAllTransactionsUseCase;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase,
                                 GetAllTransactionsUseCase getAllTransactionsUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.getAllTransactionsUseCase = getAllTransactionsUseCase;

    }
    @FeatureEnabled("create-transaction")
    @TrackMetric("transactions.created")
    @PostMapping
    @Operation(
        summary = "Create a new transaction",
        description = "Creates a new financial transaction (expense or revenue) in the ledger"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Transaction created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Transaction.class),
                examples = @ExampleObject(
                    name = "Expense Transaction",
                    value = """
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "date": "2024-01-15T10:30:00",
                          "amount": 25.50,
                          "description": "Office supplies",
                          "type": "EXPENSE"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                        {
                          "error": "Amount must be positive"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Feature is disabled",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Feature Disabled",
                    value = """
                        {
                          "error": "Feature is disabled"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<Transaction> createTransaction(
        @Parameter(description = "Transaction details", required = true)
        @RequestBody CreateTransactionRequest request) {

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
    @TrackMetric("transactions.fetched")
    @GetMapping
    @Operation(
        summary = "Get all transactions",
        description = "Retrieves all transactions from the ledger"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Transactions retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Transaction.class),
                examples = @ExampleObject(
                    name = "Transaction List",
                    value = """
                        [
                          {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "date": "2024-01-15T10:30:00",
                            "amount": 25.50,
                            "description": "Office supplies",
                            "type": "EXPENSE"
                          },
                          {
                            "id": "123e4567-e89b-12d3-a456-426614174001",
                            "date": "2024-01-15T11:00:00",
                            "amount": 1500.00,
                            "description": "Client payment",
                            "type": "REVENUE"
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Feature is disabled",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Feature Disabled",
                    value = """
                        {
                          "error": "Feature is disabled"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<List<Transaction>> getAllTransactions() {

        List<Transaction> transactions = getAllTransactionsUseCase.getAll();
        return ResponseEntity.ok(transactions);
    }
}

