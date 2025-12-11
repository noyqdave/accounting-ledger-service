package com.example.ledger.adapters.out.persistence;

import com.example.ledger.adapters.out.persistence.entity.IdempotencyEntity;
import com.example.ledger.application.port.IdempotencyRepositoryPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Database-backed implementation of IdempotencyRepositoryPort.
 * 
 * This adapter stores idempotency keys and their responses in the database.
 * Responses are stored with expiration times (default 24 hours) for automatic cleanup.
 */
@Component
@Primary
public class DatabaseIdempotencyAdapter implements IdempotencyRepositoryPort {

    private static final int DEFAULT_TTL_HOURS = 24;

    private final IdempotencyJpaRepository jpaRepository;

    public DatabaseIdempotencyAdapter(IdempotencyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<IdempotencyResponse> getCachedResponse(String idempotencyKey, String requestHash) {
        if (idempotencyKey == null || requestHash == null) {
            return Optional.empty();
        }

        Optional<IdempotencyEntity> entity = jpaRepository.findByIdempotencyKeyAndRequestHash(idempotencyKey, requestHash);
        
        if (entity.isEmpty()) {
            return Optional.empty();
        }

        IdempotencyEntity found = entity.get();
        
        // Check if expired
        if (found.getExpiresAt().isBefore(LocalDateTime.now())) {
            jpaRepository.delete(found);
            return Optional.empty();
        }

        return Optional.of(new IdempotencyResponse(found.getStatusCode(), found.getResponseBody()));
    }

    @Override
    public void storeResponse(String idempotencyKey, String requestHash, IdempotencyResponse response) {
        if (idempotencyKey == null || requestHash == null || response == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(DEFAULT_TTL_HOURS);

        IdempotencyEntity entity = new IdempotencyEntity();
        entity.setIdempotencyKey(idempotencyKey);
        entity.setRequestHash(requestHash);
        entity.setStatusCode(response.getStatusCode());
        entity.setResponseBody(response.getResponseBody());
        entity.setCreatedAt(now);
        entity.setExpiresAt(expiresAt);

        jpaRepository.save(entity);
    }

    @Override
    public boolean isValidKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return false;
        }

        // Validate UUID format
        try {
            UUID.fromString(idempotencyKey);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean hasKeyWithDifferentHash(String idempotencyKey, String requestHash) {
        if (idempotencyKey == null || requestHash == null) {
            return false;
        }

        // Check if key exists with a different hash
        return jpaRepository.existsByIdempotencyKeyAndRequestHashNot(idempotencyKey, requestHash);
    }
}


