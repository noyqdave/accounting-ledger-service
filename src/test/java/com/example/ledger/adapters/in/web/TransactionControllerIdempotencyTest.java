package com.example.ledger.adapters.in.web;

import com.example.ledger.application.usecase.CreateTransactionUseCase;
import com.example.ledger.application.usecase.GetAllTransactionsUseCase;
import com.example.ledger.domain.model.Transaction;
import com.example.ledger.domain.model.TransactionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for transaction creation with idempotency key header.
 * Inspired by the BDD scenario: "Successfully Create Transaction with Idempotency Key"
 * 
 * Note: These tests cover all examples from the scenario outline:
 * - Basic expense with idempotency (Office supplies)
 * - Revenue with idempotency (Client payment)
 * - Decimal amounts with idempotency (Coffee and snacks)
 * 
 * IMPORTANT: These are "smoke tests" - they verify that the Idempotency-Key header
 * can be sent without breaking the API. They do NOT test actual idempotency behavior
 * because idempotency logic has not been implemented yet. The controller currently
 * ignores the header entirely, and the use cases are mocked.
 * 
 * Real idempotency behavior will be tested in:
 * - BDD scenarios (idempotency.feature) - which currently fail as expected
 * - Integration tests with real idempotency implementation
 * 
 * Uses JUnit 4 to match Surefire configuration for Cucumber compatibility.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "feature.create-transaction.enabled=true",
        "feature.get-all-transactions.enabled=true"
})
public class TransactionControllerIdempotencyTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateTransactionUseCase createTransactionUseCase;

    @MockBean
    private GetAllTransactionsUseCase getAllTransactionsUseCase;

    @Test
    public void shouldCreateExpenseTransactionWithIdempotencyKey_OfficeSupplies() throws Exception {
        // Arrange - Test basic expense with idempotency
        String idempotencyKey = "550e8400-e29b-41d4-a716-446655440000";
        BigDecimal amount = new BigDecimal("100.00");
        String description = "Office supplies";
        TransactionType type = TransactionType.EXPENSE;

        Transaction savedTransaction = new Transaction(
                UUID.randomUUID(),
                LocalDateTime.now(),
                amount,
                description,
                type
        );

        when(createTransactionUseCase.create(any(Transaction.class)))
                .thenReturn(savedTransaction);

        String requestJson = """
        {
          "amount": 100.00,
          "description": "Office supplies",
          "type": "EXPENSE"
        }
        """;

        // Act & Assert
        mockMvc.perform(
                        post("/transactions")
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.date").exists());
    }

    @Test
    public void shouldCreateRevenueTransactionWithIdempotencyKey_ClientPayment() throws Exception {
        // Arrange - Test revenue with idempotency
        String idempotencyKey = "660e8400-e29b-41d4-a716-446655440001";
        BigDecimal amount = new BigDecimal("2500.00");
        String description = "Client payment";
        TransactionType type = TransactionType.REVENUE;

        Transaction savedTransaction = new Transaction(
                UUID.randomUUID(),
                LocalDateTime.now(),
                amount,
                description,
                type
        );

        when(createTransactionUseCase.create(any(Transaction.class)))
                .thenReturn(savedTransaction);

        String requestJson = """
        {
          "amount": 2500.00,
          "description": "Client payment",
          "type": "REVENUE"
        }
        """;

        // Act & Assert
        mockMvc.perform(
                        post("/transactions")
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(2500.00))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.type").value("REVENUE"))
                .andExpect(jsonPath("$.date").exists());
    }

    @Test
    public void shouldCreateExpenseTransactionWithIdempotencyKey_DecimalAmount() throws Exception {
        // Arrange - Test decimal amounts with idempotency
        String idempotencyKey = "770e8400-e29b-41d4-a716-446655440002";
        BigDecimal amount = new BigDecimal("99.99");
        String description = "Coffee and snacks";
        TransactionType type = TransactionType.EXPENSE;

        Transaction savedTransaction = new Transaction(
                UUID.randomUUID(),
                LocalDateTime.now(),
                amount,
                description,
                type
        );

        when(createTransactionUseCase.create(any(Transaction.class)))
                .thenReturn(savedTransaction);

        String requestJson = """
        {
          "amount": 99.99,
          "description": "Coffee and snacks",
          "type": "EXPENSE"
        }
        """;

        // Act & Assert
        mockMvc.perform(
                        post("/transactions")
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.date").exists());
    }
}
