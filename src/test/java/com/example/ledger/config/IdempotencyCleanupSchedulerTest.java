package com.example.ledger.config;

import com.example.ledger.adapters.out.scheduling.IdempotencyCleanupScheduler;
import com.example.ledger.application.port.IdempotencyRepositoryPort;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Unit tests for IdempotencyCleanupScheduler.
 * 
 * Tests verify that the scheduler correctly calls the repository port
 * and handles exceptions gracefully.
 */
@RunWith(MockitoJUnitRunner.class)
public class IdempotencyCleanupSchedulerTest {

    @Mock
    private IdempotencyRepositoryPort idempotencyRepository;

    private IdempotencyCleanupScheduler scheduler;

    @Before
    public void setUp() {
        // This will fail compilation - class doesn't exist yet (Red phase)
        scheduler = new IdempotencyCleanupScheduler(idempotencyRepository);
    }

    @Test
    public void shouldCallDeleteExpiredKeysWhenCleanupRuns() {
        // Act
        scheduler.cleanupExpiredKeys();

        // Assert
        verify(idempotencyRepository, times(1)).deleteExpiredKeys();
    }

    @Test
    public void shouldHandleExceptionsGracefully() {
        // Arrange
        doThrow(new RuntimeException("Database error")).when(idempotencyRepository).deleteExpiredKeys();

        // Act - Should not throw exception
        scheduler.cleanupExpiredKeys();

        // Assert - Method was called despite exception
        verify(idempotencyRepository, times(1)).deleteExpiredKeys();
    }
}
