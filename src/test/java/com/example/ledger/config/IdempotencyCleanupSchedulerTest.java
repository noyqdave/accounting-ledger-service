package com.example.ledger.config;

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
 * Following TDD: This test is written first and should fail because
 * the IdempotencyCleanupScheduler class doesn't exist yet.
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
