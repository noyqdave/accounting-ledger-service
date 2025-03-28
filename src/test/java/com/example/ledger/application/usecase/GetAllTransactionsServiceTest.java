package com.example.ledger.application.usecase;

import com.example.ledger.config.FeatureFlags;
import com.example.ledger.domain.model.Transaction;
import com.example.ledger.domain.model.TransactionType;
import com.example.ledger.domain.port.TransactionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetAllTransactionsServiceTest {

    private TransactionRepositoryPort repository;
    private FeatureFlags featureFlags;
    private GetAllTransactionsService service;

    @BeforeEach
    void setUp() {
        repository = mock(TransactionRepositoryPort.class);
        featureFlags = mock(FeatureFlags.class);
        service = new GetAllTransactionsService(repository, featureFlags);
    }

    @Test
    void shouldReturnListOfTransactionsWhenFeatureIsEnabled() {
        // Arrange
        when(featureFlags.isGetAllTransactionsEnabled()).thenReturn(true);

        Transaction t1 = new Transaction(UUID.randomUUID(), LocalDateTime.now(), new BigDecimal("10.00"), "Test", TransactionType.EXPENSE);
        Transaction t2 = new Transaction(UUID.randomUUID(), LocalDateTime.now(), new BigDecimal("20.00"), "Test2", TransactionType.REVENUE);

        when(repository.findAll()).thenReturn(List.of(t1, t2));

        // Act
        List<Transaction> results = service.getAll();

        // Assert
        assertEquals(2, results.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void shouldThrowExceptionWhenFeatureIsDisabled() {
        when(featureFlags.isGetAllTransactionsEnabled()).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.getAll());
        assertEquals("Fetching all transactions is currently disabled", ex.getMessage());

        verify(repository, never()).findAll();
    }
}
