package com.example.ledger.application.usecase;

import com.example.ledger.domain.model.Transaction;
import com.example.ledger.domain.model.TransactionType;
import com.example.ledger.domain.port.TransactionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateTransactionServiceTest {

    private TransactionRepositoryPort repository;
    private CreateTransactionService service;

    @BeforeEach
    void setUp() {
        repository = mock(TransactionRepositoryPort.class);
        service = new CreateTransactionService(repository);
    }

    @Test
    void shouldSaveTransactionToRepository() {
        // Arrange
        Transaction input = new Transaction(
                new BigDecimal("99.99"),
                "Test transaction",
                TransactionType.REVENUE
        );

        Transaction saved = new Transaction(
                UUID.randomUUID(),
                LocalDateTime.now(),
                input.getAmount(),
                input.getDescription(),
                input.getType()
        );

        when(repository.save(input)).thenReturn(saved);

        // Act
        Transaction result = service.create(input);

        // Assert
        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals("Test transaction", result.getDescription());
        verify(repository, times(1)).save(input);
    }
}
