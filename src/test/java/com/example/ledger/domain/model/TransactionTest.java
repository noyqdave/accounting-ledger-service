package com.example.ledger.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {

    @Test
    void shouldCreateRevenueTransactionWithValidData() {
        Transaction transaction = new Transaction(
                new BigDecimal("100.00"),
                "Client payment",
                TransactionType.REVENUE
        );

        assertEquals("Client payment", transaction.getDescription());
        assertEquals(TransactionType.REVENUE, transaction.getType());
        assertTrue(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(transaction.getId());
        assertNotNull(transaction.getDate());
    }

    @Test
    void shouldCreateExpenseTransaction() {
        Transaction transaction = new Transaction(
                new BigDecimal("25.00"),
                "Office supplies",
                TransactionType.EXPENSE
        );

        assertEquals(TransactionType.EXPENSE, transaction.getType());
    }

    @Test
    void shouldThrowIfAmountIsZero() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Transaction(BigDecimal.ZERO, "Invalid", TransactionType.REVENUE)
        );

        assertEquals("Amount must be positive", ex.getMessage());
    }

    @Test
    void shouldThrowIfAmountIsNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Transaction(new BigDecimal("-10.00"), "Invalid", TransactionType.EXPENSE)
        );

        assertEquals("Amount must be positive", ex.getMessage());
    }

    @Test
    void shouldThrowIfDescriptionIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Transaction(new BigDecimal("50.00"), null, TransactionType.REVENUE)
        );

        assertEquals("Description must not be null or empty", ex.getMessage());
    }

}
