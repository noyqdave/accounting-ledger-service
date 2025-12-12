package com.example.ledger.adapters.out.persistence;

import com.example.ledger.adapters.out.persistence.entity.IdempotencyEntity;
import com.example.ledger.application.port.IdempotencyRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Database-backed implementation of IdempotencyRepositoryPort.
 * 
 * This adapter stores idempotency keys and their responses in a database table.
 * It handles expiration checking and cleanup of expired keys.
 */
@Component
public class DatabaseIdempotencyAdapter implements IdempotencyRepositoryPort {

    private static final int DEFAULT_TTL_HOURS = 24;
    
    private final IdempotencyJpaRepository idempotencyJpaRepository;

    public DatabaseIdempotencyAdapter(IdempotencyJpaRepository idempotencyJpaRepository) {
        this.idempotencyJpaRepository = idempotencyJpaRepository;
    }

    @Override
    public Optional<IdempotencyResponse> getCachedResponse(String idempotencyKey, String requestHash) {
        if (idempotencyKey == null || requestHash == null) {
            return Optional.empty();
        }

        Optional<IdempotencyEntity> entityOpt = idempotencyJpaRepository
                .findByIdempotencyKeyAndRequestHash(idempotencyKey, requestHash);

        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        IdempotencyEntity entity = entityOpt.get();
        
        // Check if expired - if so, delete and return empty
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            idempotencyJpaRepository.delete(entity);
            return Optional.empty();
        }

        // Return cached response
        IdempotencyResponse response = new IdempotencyResponse(
                entity.getStatusCode(),
                entity.getResponseBody()
        );
        return Optional.of(response);
    }

    @Override
    public void storeResponse(String idempotencyKey, String requestHash, IdempotencyResponse response) {
        if (idempotencyKey == null || requestHash == null || response == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(DEFAULT_TTL_HOURS);

        // Check if entity already exists
        Optional<IdempotencyEntity> existingEntityOpt = idempotencyJpaRepository
                .findByIdempotencyKeyAndRequestHash(idempotencyKey, requestHash);

        IdempotencyEntity entity;
        if (existingEntityOpt.isPresent()) {
            // Update existing entity
            entity = existingEntityOpt.get();
        } else {
            // Create new entity
            entity = new IdempotencyEntity();
            entity.setIdempotencyKey(idempotencyKey);
            entity.setRequestHash(requestHash);
            entity.setCreatedAt(now);
        }

        entity.setStatusCode(response.getStatusCode());
        entity.setResponseBody(response.getResponseBody());
        entity.setExpiresAt(expiresAt);

        idempotencyJpaRepository.save(entity);
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
        return idempotencyJpaRepository.existsByIdempotencyKeyAndRequestHashNot(idempotencyKey, requestHash);
    }

    @Override
    public void deleteExpiredKeys() {
        LocalDateTime now = LocalDateTime.now();
        idempotencyJpaRepository.deleteByExpiresAtBefore(now);
    }
}
