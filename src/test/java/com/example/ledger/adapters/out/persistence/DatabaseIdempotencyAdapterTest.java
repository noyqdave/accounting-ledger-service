package com.example.ledger.adapters.out.persistence;

import com.example.ledger.application.port.IdempotencyRepositoryPort;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.ledger.adapters.out.persistence.entity.IdempotencyEntity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
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
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
public class DatabaseIdempotencyAdapterTest {

    @Autowired
    private IdempotencyRepositoryPort idempotencyRepository;

    @Autowired
    private IdempotencyJpaRepository idempotencyJpaRepository;

    @Before
    public void setUp() {
        // Clean up database before each test to avoid unique constraint violations
        // when multiple tests use the same idempotency key and request hash
        idempotencyJpaRepository.deleteAll();
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
        assertEquals("Cached response should match original response exactly",
                originalResponse.getResponseBody(), cachedResponse.get().getResponseBody());
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
     * Test: hasKeyWithDifferentHash should return true when key exists with different hash.
     * 
     * This is critical for conflict detection - when a client tries to reuse an idempotency key
     * with a different request body, the filter uses this method to detect the conflict
     * and return 409 Conflict.
     */
    @Test
    public void shouldReturnTrueWhenKeyExistsWithDifferentHash() {
        // Arrange - Store response for first request
        String idempotencyKey = "880e8400-e29b-41d4-a716-446655440003";
        String originalRequestHash = hashRequest(100.00, "Office supplies", "EXPENSE");
        String originalResponseBody = "{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"amount\":100.00,\"description\":\"Office supplies\",\"type\":\"EXPENSE\"}";
        
        IdempotencyRepositoryPort.IdempotencyResponse originalResponse = 
                new IdempotencyRepositoryPort.IdempotencyResponse(200, originalResponseBody);

        idempotencyRepository.storeResponse(idempotencyKey, originalRequestHash, originalResponse);

        // Act - Check for conflict with different request hash
        String differentRequestHash = hashRequest(200.00, "Office supplies", "EXPENSE"); // Different amount
        boolean hasConflict = idempotencyRepository.hasKeyWithDifferentHash(idempotencyKey, differentRequestHash);

        // Assert - Should detect conflict
        assertTrue("Should detect conflict when key exists with different request hash",
                hasConflict);
    }

    /**
     * Test: hasKeyWithDifferentHash should return false when key doesn't exist.
     */
    @Test
    public void shouldReturnFalseWhenKeyDoesNotExist() {
        // Arrange - No stored responses
        String idempotencyKey = "880e8400-e29b-41d4-a716-446655440003";
        String requestHash = hashRequest(100.00, "Office supplies", "EXPENSE");

        // Act
        boolean hasConflict = idempotencyRepository.hasKeyWithDifferentHash(idempotencyKey, requestHash);

        // Assert
        assertFalse("Should return false when key doesn't exist",
                hasConflict);
    }

    /**
     * Test: hasKeyWithDifferentHash should return false when key exists with same hash.
     */
    @Test
    public void shouldReturnFalseWhenKeyExistsWithSameHash() {
        // Arrange - Store response
        String idempotencyKey = "880e8400-e29b-41d4-a716-446655440003";
        String requestHash = hashRequest(100.00, "Office supplies", "EXPENSE");
        String responseBody = "{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"amount\":100.00,\"description\":\"Office supplies\",\"type\":\"EXPENSE\"}";
        
        IdempotencyRepositoryPort.IdempotencyResponse response = 
                new IdempotencyRepositoryPort.IdempotencyResponse(200, responseBody);

        idempotencyRepository.storeResponse(idempotencyKey, requestHash, response);

        // Act - Check with same hash
        boolean hasConflict = idempotencyRepository.hasKeyWithDifferentHash(idempotencyKey, requestHash);

        // Assert - Should not detect conflict (same hash means same request)
        assertFalse("Should return false when key exists with same request hash",
                hasConflict);
    }

    /**
     * Test: hasKeyWithDifferentHash should handle null parameters gracefully.
     */
    @Test
    public void shouldHandleNullParametersInHasKeyWithDifferentHash() {
        // Act & Assert
        assertFalse("Should return false when idempotency key is null",
                idempotencyRepository.hasKeyWithDifferentHash(null, "some-hash"));
        assertFalse("Should return false when request hash is null",
                idempotencyRepository.hasKeyWithDifferentHash("880e8400-e29b-41d4-a716-446655440003", null));
        assertFalse("Should return false when both parameters are null",
                idempotencyRepository.hasKeyWithDifferentHash(null, null));
    }

    /**
     * Test: Expired entries should not be returned and should be deleted.
     * 
     * This tests the TTL/expiration behavior - entries older than 24 hours
     * should be automatically cleaned up.
     */
    @Test
    public void shouldNotReturnExpiredEntries() {
        // Arrange - Create an expired entry directly in the database
        String idempotencyKey = "880e8400-e29b-41d4-a716-446655440003";
        String requestHash = hashRequest(100.00, "Office supplies", "EXPENSE");
        
        IdempotencyEntity expiredEntity = new IdempotencyEntity();
        expiredEntity.setIdempotencyKey(idempotencyKey);
        expiredEntity.setRequestHash(requestHash);
        expiredEntity.setStatusCode(200);
        expiredEntity.setResponseBody("{\"id\":\"123e4567-e89b-12d3-a456-426614174000\"}");
        expiredEntity.setCreatedAt(LocalDateTime.now().minusHours(25)); // Expired (older than 24 hours)
        expiredEntity.setExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        
        idempotencyJpaRepository.save(expiredEntity);

        // Act - Try to retrieve expired entry
        Optional<IdempotencyRepositoryPort.IdempotencyResponse> cachedResponse = 
                idempotencyRepository.getCachedResponse(idempotencyKey, requestHash);

        // Assert - Should return empty and delete the expired entry
        assertFalse("Expired entries should not be returned",
                cachedResponse.isPresent());
        
        // Verify entry was deleted
        assertFalse("Expired entry should be deleted from database",
                idempotencyJpaRepository.findByIdempotencyKeyAndRequestHash(idempotencyKey, requestHash).isPresent());
    }

    /**
     * Test: Non-expired entries should be returned.
     */
    @Test
    public void shouldReturnNonExpiredEntries() {
        // Arrange - Store a response (should have default 24-hour TTL)
        String idempotencyKey = "880e8400-e29b-41d4-a716-446655440003";
        String requestHash = hashRequest(100.00, "Office supplies", "EXPENSE");
        String responseBody = "{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"amount\":100.00,\"description\":\"Office supplies\",\"type\":\"EXPENSE\"}";
        
        IdempotencyRepositoryPort.IdempotencyResponse response = 
                new IdempotencyRepositoryPort.IdempotencyResponse(200, responseBody);

        idempotencyRepository.storeResponse(idempotencyKey, requestHash, response);

        // Act - Retrieve immediately (should not be expired)
        Optional<IdempotencyRepositoryPort.IdempotencyResponse> cachedResponse = 
                idempotencyRepository.getCachedResponse(idempotencyKey, requestHash);

        // Assert - Should return the cached response
        assertTrue("Non-expired entries should be returned",
                cachedResponse.isPresent());
        assertEquals(200, cachedResponse.get().getStatusCode());
        assertEquals(responseBody, cachedResponse.get().getResponseBody());
    }

    /**
     * Test: storeResponse should handle null parameters gracefully.
     */
    @Test
    public void shouldHandleNullParametersInStoreResponse() {
        // Arrange
        String idempotencyKey = "880e8400-e29b-41d4-a716-446655440003";
        String requestHash = hashRequest(100.00, "Office supplies", "EXPENSE");
        IdempotencyRepositoryPort.IdempotencyResponse response = 
                new IdempotencyRepositoryPort.IdempotencyResponse(200, "{\"id\":\"123\"}");

        // Act & Assert - Should not throw exceptions
        idempotencyRepository.storeResponse(null, requestHash, response);
        idempotencyRepository.storeResponse(idempotencyKey, null, response);
        idempotencyRepository.storeResponse(idempotencyKey, requestHash, null);
        idempotencyRepository.storeResponse(null, null, null);

        // Verify nothing was stored
        Optional<IdempotencyRepositoryPort.IdempotencyResponse> cachedResponse = 
                idempotencyRepository.getCachedResponse(idempotencyKey, requestHash);
        assertFalse("Null parameters should not store anything",
                cachedResponse.isPresent());
    }

    /**
     * Test: getCachedResponse should handle null parameters gracefully.
     */
    @Test
    public void shouldHandleNullParametersInGetCachedResponse() {
        // Act & Assert - Should return empty Optional for null parameters
        Optional<IdempotencyRepositoryPort.IdempotencyResponse> result1 = 
                idempotencyRepository.getCachedResponse(null, "some-hash");
        assertFalse("Should return empty when idempotency key is null", result1.isPresent());

        Optional<IdempotencyRepositoryPort.IdempotencyResponse> result2 = 
                idempotencyRepository.getCachedResponse("880e8400-e29b-41d4-a716-446655440003", null);
        assertFalse("Should return empty when request hash is null", result2.isPresent());

        Optional<IdempotencyRepositoryPort.IdempotencyResponse> result3 = 
                idempotencyRepository.getCachedResponse(null, null);
        assertFalse("Should return empty when both parameters are null", result3.isPresent());
    }

    /**
     * Test: Multiple entries with same key but different hashes should be handled correctly.
     * 
     * This tests the scenario where a key has been used with multiple different request bodies.
     * The repository should correctly identify conflicts for each unique hash combination.
     */
    @Test
    public void shouldHandleMultipleEntriesWithSameKeyButDifferentHashes() {
        // Arrange - Store multiple responses with same key but different request hashes
        String idempotencyKey = "880e8400-e29b-41d4-a716-446655440003";
        
        String hash1 = hashRequest(100.00, "Office supplies", "EXPENSE");
        String hash2 = hashRequest(200.00, "Office supplies", "EXPENSE");
        String hash3 = hashRequest(100.00, "Different description", "EXPENSE");
        
        IdempotencyRepositoryPort.IdempotencyResponse response1 = 
                new IdempotencyRepositoryPort.IdempotencyResponse(200, "{\"id\":\"111\",\"amount\":100.00}");
        IdempotencyRepositoryPort.IdempotencyResponse response2 = 
                new IdempotencyRepositoryPort.IdempotencyResponse(200, "{\"id\":\"222\",\"amount\":200.00}");
        IdempotencyRepositoryPort.IdempotencyResponse response3 = 
                new IdempotencyRepositoryPort.IdempotencyResponse(200, "{\"id\":\"333\",\"description\":\"Different\"}");

        // Store all three responses
        idempotencyRepository.storeResponse(idempotencyKey, hash1, response1);
        idempotencyRepository.storeResponse(idempotencyKey, hash2, response2);
        idempotencyRepository.storeResponse(idempotencyKey, hash3, response3);

        // Act & Assert - Each hash should retrieve its own response
        Optional<IdempotencyRepositoryPort.IdempotencyResponse> cached1 = 
                idempotencyRepository.getCachedResponse(idempotencyKey, hash1);
        assertTrue("Should retrieve response for hash1", cached1.isPresent());
        assertTrue("Response should contain correct ID", cached1.get().getResponseBody().contains("111"));

        Optional<IdempotencyRepositoryPort.IdempotencyResponse> cached2 = 
                idempotencyRepository.getCachedResponse(idempotencyKey, hash2);
        assertTrue("Should retrieve response for hash2", cached2.isPresent());
        assertTrue("Response should contain correct ID", cached2.get().getResponseBody().contains("222"));

        Optional<IdempotencyRepositoryPort.IdempotencyResponse> cached3 = 
                idempotencyRepository.getCachedResponse(idempotencyKey, hash3);
        assertTrue("Should retrieve response for hash3", cached3.isPresent());
        assertTrue("Response should contain correct ID", cached3.get().getResponseBody().contains("333"));

        // Test conflict detection - hash1 should detect hash2 and hash3 as conflicts
        assertTrue("hash1 should detect hash2 as conflict",
                idempotencyRepository.hasKeyWithDifferentHash(idempotencyKey, hash1));
        assertTrue("hash2 should detect hash1 as conflict",
                idempotencyRepository.hasKeyWithDifferentHash(idempotencyKey, hash2));
        assertTrue("hash3 should detect hash1 as conflict",
                idempotencyRepository.hasKeyWithDifferentHash(idempotencyKey, hash3));
    }

    /**
     * Test: Cleanup should delete expired keys.
     * 
     * This test verifies that expired idempotency keys are removed during cleanup.
     */
    @Test
    public void shouldDeleteExpiredKeys() {
        // Arrange - Create expired entity
        String expiredKey = "expired-key-001";
        String requestHash = hashRequest(100.00, "Test", "EXPENSE");
        createExpiredEntity(expiredKey, requestHash);
        
        // Act - Call cleanup method
        idempotencyRepository.deleteExpiredKeys();
        
        // Assert - Expired entity should be deleted
        assertFalse("Expired entity should be deleted",
                idempotencyJpaRepository.findByIdempotencyKeyAndRequestHash(expiredKey, requestHash).isPresent());
    }
    
    /**
     * Test: Cleanup should preserve active (non-expired) keys.
     * 
     * This test verifies that active idempotency keys are not deleted during cleanup.
     */
    @Test
    public void shouldPreserveActiveKeys() {
        // Arrange - Create active entity
        String activeKey = "active-key-001";
        String requestHash = hashRequest(100.00, "Test", "EXPENSE");
        createActiveEntity(activeKey, requestHash);
        
        // Act - Call cleanup method
        idempotencyRepository.deleteExpiredKeys();
        
        // Assert - Active entity should remain
        assertTrue("Active entity should remain",
                idempotencyJpaRepository.findByIdempotencyKeyAndRequestHash(activeKey, requestHash).isPresent());
    }
    
    private void createExpiredEntity(String key, String requestHash) {
        IdempotencyEntity entity = new IdempotencyEntity();
        entity.setIdempotencyKey(key);
        entity.setRequestHash(requestHash);
        entity.setStatusCode(200);
        entity.setResponseBody("{\"id\":\"expired\"}");
        entity.setCreatedAt(LocalDateTime.now().minusHours(25));
        entity.setExpiresAt(LocalDateTime.now().minusHours(1));
        idempotencyJpaRepository.save(entity);
    }
    
    private void createActiveEntity(String key, String requestHash) {
        IdempotencyEntity entity = new IdempotencyEntity();
        entity.setIdempotencyKey(key);
        entity.setRequestHash(requestHash);
        entity.setStatusCode(200);
        entity.setResponseBody("{\"id\":\"active\"}");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusHours(23));
        idempotencyJpaRepository.save(entity);
    }

    /**
     * Helper method to hash request body for request matching.
     * Uses SHA-256 hashing to match the actual implementation in IdempotencyFilter.
     * This ensures test hashes match production hashes for accurate testing.
     */
    private String hashRequest(double amount, String description, String type) {
        String requestBody = String.format("amount=%.2f&description=%s&type=%s", amount, description, type);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(requestBody.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash code if SHA-256 is unavailable (should never happen)
            return String.valueOf(requestBody.hashCode());
        }
    }
}
