package com.example.ledger.adapters.out.persistence;

import com.example.ledger.application.port.IdempotencyRepositoryPort;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Unit tests for DatabaseIdempotencyAdapter.
 * Inspired by the BDD scenario: "Retry Request with Same Idempotency Key Returns Same Response"
 * 
 * These tests verify the database-backed idempotency repository operations.
 * 
 * Uses JUnit 4 to match Surefire configuration for Cucumber compatibility.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
public class DatabaseIdempotencyAdapterTest {

    @Autowired
    private IdempotencyRepositoryPort idempotencyRepository;

    @Before
    public void setUp() {
        // Database is automatically initialized by Spring Boot Test
    }

    /**
     * Test: First request with a new idempotency key should not have a cached response.
     * 
     * This tests the initial state when a client sends a request with an idempotency key
     * that hasn't been seen before. The service should return empty Optional, allowing
     * the request to proceed normally.
     */
    @Test
    public void shouldReturnEmptyOptionalWhenIdempotencyKeyNotFound() {
        // Arrange - First request with a new idempotency key
        String idempotencyKey = "880e8400-e29b-41d4-a716-446655440003";
        String requestHash = hashRequest(100.00, "Office supplies", "EXPENSE");

        // Act
        Optional<IdempotencyRepositoryPort.IdempotencyResponse> cachedResponse = 
                idempotencyRepository.getCachedResponse(idempotencyKey, requestHash);

        // Assert
        assertFalse("First request with new idempotency key should not have cached response",
                cachedResponse.isPresent());
    }

    /**
     * Test: Service should be able to store a response for an idempotency key.
     * 
     * After a successful transaction creation, the response should be stored
     * so that subsequent requests with the same key can return the cached response.
     */
    @Test
    public void shouldStoreResponseForIdempotencyKey() {
        // Arrange - First request creates and stores response
        String idempotencyKey = "880e8400-e29b-41d4-a716-446655440003";
        String requestHash = hashRequest(100.00, "Office supplies", "EXPENSE");
        String responseBody = "{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"amount\":100.00,\"description\":\"Office supplies\",\"type\":\"EXPENSE\"}";
        
        IdempotencyRepositoryPort.IdempotencyResponse response = 
                new IdempotencyRepositoryPort.IdempotencyResponse(200, responseBody);

        // Act
        idempotencyRepository.storeResponse(idempotencyKey, requestHash, response);

        // Assert - Verify response was stored (by retrieving it)
        Optional<IdempotencyRepositoryPort.IdempotencyResponse> cachedResponse = 
                idempotencyRepository.getCachedResponse(idempotencyKey, requestHash);
        assertTrue("Response should be stored and retrievable after storing",
                cachedResponse.isPresent());
        assertEquals(200, cachedResponse.get().getStatusCode());
        assertEquals(responseBody, cachedResponse.get().getResponseBody());
    }

    /**
     * Test: Retry request with same idempotency key and same request body should return cached response.
     * 
     * This is the core idempotency behavior - when a client retries a request with the same
     * idempotency key and same request body, they should get back the exact same response
     * (including the same transaction ID) without creating a duplicate transaction.
     * 
     * This test is inspired by: "Retry Request with Same Idempotency Key Returns Same Response"
     */
    @Test
    public void shouldReturnCachedResponseWhenSameIdempotencyKeyAndRequestHash() {
        // Arrange - Simulate retry scenario: first request, then second request with same key
        String idempotencyKey = "880e8400-e29b-41d4-a716-446655440003";
        String requestHash = hashRequest(100.00, "Office supplies", "EXPENSE");
        String transactionId = "123e4567-e89b-12d3-a456-426614174000";
        String responseBody = String.format(
                "{\"id\":\"%s\",\"amount\":100.00,\"description\":\"Office supplies\",\"type\":\"EXPENSE\"}",
                transactionId);
        
        IdempotencyRepositoryPort.IdempotencyResponse originalResponse = 
                new IdempotencyRepositoryPort.IdempotencyResponse(200, responseBody);

        // Store the original response (simulating first request)
        idempotencyRepository.storeResponse(idempotencyKey, requestHash, originalResponse);

        // Act - Retry request with same idempotency key and same request hash
        Optional<IdempotencyRepositoryPort.IdempotencyResponse> cachedResponse = 
                idempotencyRepository.getCachedResponse(idempotencyKey, requestHash);

        // Assert - Should return the same cached response with identical transaction ID
        assertTrue("Retry request with same idempotency key should return cached response",
                cachedResponse.isPresent());
        assertEquals(200, cachedResponse.get().getStatusCode());
        assertEquals(originalResponse.getResponseBody(), cachedResponse.get().getResponseBody(),
                "Cached response should match original response exactly");
        assertTrue("Cached response should contain the same transaction ID",
                cachedResponse.get().getResponseBody().contains(transactionId));
    }

    /**
     * Test: Request with same idempotency key but different request body should not match.
     * 
     * This tests conflict detection - if a client tries to reuse an idempotency key
     * with a different request body, the service should detect this and return empty
     * (the filter will then return 409 Conflict).
     */
    @Test
    public void shouldReturnEmptyOptionalWhenSameKeyButDifferentRequestHash() {
        // Arrange - Store response for first request
        String idempotencyKey = "880e8400-e29b-41d4-a716-446655440003";
        String originalRequestHash = hashRequest(100.00, "Office supplies", "EXPENSE");
        String originalResponseBody = "{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"amount\":100.00,\"description\":\"Office supplies\",\"type\":\"EXPENSE\"}";
        
        IdempotencyRepositoryPort.IdempotencyResponse originalResponse = 
                new IdempotencyRepositoryPort.IdempotencyResponse(200, originalResponseBody);

        idempotencyRepository.storeResponse(idempotencyKey, originalRequestHash, originalResponse);

        // Act - Request with same key but different request body (different hash)
        String differentRequestHash = hashRequest(200.00, "Office supplies", "EXPENSE"); // Different amount

        Optional<IdempotencyRepositoryPort.IdempotencyResponse> cachedResponse = 
                idempotencyRepository.getCachedResponse(idempotencyKey, differentRequestHash);

        // Assert - Should return empty (indicating conflict, which filter will handle as 409)
        assertFalse("Request with same idempotency key but different request body should not match cached response",
                cachedResponse.isPresent());
    }

    /**
     * Test: Service should validate idempotency key format.
     * 
     * Idempotency keys should be validated (e.g., UUID format) to prevent
     * malformed keys from being processed.
     */
    @Test
    public void shouldValidateIdempotencyKeyFormat() {
        // Arrange
        String validKey = "880e8400-e29b-41d4-a716-446655440003"; // Valid UUID format
        String invalidKey = "not-a-valid-uuid";

        // Act & Assert
        assertTrue("Valid UUID format should be accepted",
                idempotencyRepository.isValidKey(validKey));
        assertFalse("Invalid UUID format should be rejected",
                idempotencyRepository.isValidKey(invalidKey));
        assertFalse("Null idempotency key should be rejected",
                idempotencyRepository.isValidKey(null));
        assertFalse("Empty idempotency key should be rejected",
                idempotencyRepository.isValidKey(""));
    }

    /**
     * Helper method to hash request body for request matching.
     * This simulates how request bodies will be hashed for idempotency matching.
     * 
     * TODO: Replace with actual hashing implementation from IdempotencyRepositoryPort or a utility class.
     * The actual implementation should use proper cryptographic hashing (e.g., SHA-256)
     * to ensure request bodies are compared accurately.
     */
    private String hashRequest(double amount, String description, String type) {
        // Simple hash simulation - actual implementation will use proper hashing (e.g., SHA-256)
        String requestBody = String.format("amount=%.2f&description=%s&type=%s", amount, description, type);
        return String.valueOf(requestBody.hashCode());
    }
}
