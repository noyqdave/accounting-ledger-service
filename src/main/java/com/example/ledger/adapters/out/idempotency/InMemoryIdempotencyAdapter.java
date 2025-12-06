package com.example.ledger.adapters.out.idempotency;

import com.example.ledger.application.port.IdempotencyRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of IdempotencyRepositoryPort.
 * 
 * This adapter stores idempotency keys and their responses in memory.
 * For production, consider using a database-backed adapter with TTL/expiration.
 */
@Component
public class InMemoryIdempotencyAdapter implements IdempotencyRepositoryPort {

    // In-memory storage: key -> (requestHash -> response)
    private final Map<String, Map<String, IdempotencyResponse>> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<IdempotencyResponse> getCachedResponse(String idempotencyKey, String requestHash) {
        if (idempotencyKey == null || requestHash == null) {
            return Optional.empty();
        }

        Map<String, IdempotencyResponse> keyResponses = storage.get(idempotencyKey);
        if (keyResponses == null) {
            return Optional.empty();
        }

        IdempotencyResponse response = keyResponses.get(requestHash);
        return response != null ? Optional.of(response) : Optional.empty();
    }

    @Override
    public void storeResponse(String idempotencyKey, String requestHash, IdempotencyResponse response) {
        if (idempotencyKey == null || requestHash == null || response == null) {
            return;
        }

        storage.computeIfAbsent(idempotencyKey, k -> new ConcurrentHashMap<>())
                .put(requestHash, response);
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

        Map<String, IdempotencyResponse> keyResponses = storage.get(idempotencyKey);
        if (keyResponses == null || keyResponses.isEmpty()) {
            return false;
        }

        // If the key exists but with a different hash, it's a conflict
        return !keyResponses.containsKey(requestHash) && !keyResponses.isEmpty();
    }
}
