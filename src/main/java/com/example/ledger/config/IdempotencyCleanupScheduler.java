package com.example.ledger.config;

import com.example.ledger.application.port.IdempotencyRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task for cleaning up expired idempotency keys.
 * 
 * Runs periodically to delete expired idempotency keys from the database,
 * preventing unbounded growth and maintaining database health.
 */
@Component
public class IdempotencyCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyCleanupScheduler.class);

    private final IdempotencyRepositoryPort idempotencyRepository;

    public IdempotencyCleanupScheduler(IdempotencyRepositoryPort idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
    }

    /**
     * Scheduled task to delete expired idempotency keys.
     * Runs every hour (3600000 milliseconds).
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredKeys() {
        long startTime = System.currentTimeMillis();
        logger.info("Starting scheduled cleanup of expired idempotency keys");
        
        try {
            idempotencyRepository.deleteExpiredKeys();
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Completed scheduled cleanup of expired idempotency keys in {} ms", duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error during scheduled cleanup of expired idempotency keys after {} ms", duration, e);
        }
    }
}
