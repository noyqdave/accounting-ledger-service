package com.example.ledger.application.port;

import java.util.Optional;

/**
 * Port for idempotency key operations.
 * 
 * This port defines the contract for storing and retrieving idempotency key responses.
 * Implementations can use in-memory storage, database, or distributed cache.
 */
public interface IdempotencyRepositoryPort {
    
    /**
     * Retrieves a cached response for the given idempotency key if it exists
     * and the request hash matches the stored request hash.
     * 
     * @param idempotencyKey The idempotency key sent by the client
     * @param requestHash The hash of the current request body
     * @return Optional containing the cached response if found and request matches,
     *         empty Optional if key doesn't exist or request differs
     */
    Optional<IdempotencyResponse> getCachedResponse(String idempotencyKey, String requestHash);
    
    /**
     * Stores a response for the given idempotency key and request hash.
     * 
     * @param idempotencyKey The idempotency key sent by the client
     * @param requestHash The hash of the request body
     * @param response The response to cache (status code and body)
     */
    void storeResponse(String idempotencyKey, String requestHash, IdempotencyResponse response);
    
    /**
     * Validates that an idempotency key has a valid format.
     * 
     * @param idempotencyKey The idempotency key to validate
     * @return true if the key format is valid, false otherwise
     */
    boolean isValidKey(String idempotencyKey);
    
    /**
     * Check if an idempotency key exists with a different request hash.
     * This is used to detect conflicts (same key, different request).
     * 
     * @param idempotencyKey The idempotency key to check
     * @param requestHash The hash of the current request body
     * @return true if the key exists with a different hash, false otherwise
     */
    boolean hasKeyWithDifferentHash(String idempotencyKey, String requestHash);
    
    /**
     * Deletes all expired idempotency keys from storage.
     * This method is used by scheduled cleanup tasks to maintain database health.
     */
    void deleteExpiredKeys();
    
    /**
     * Response data stored for idempotency.
     */
    class IdempotencyResponse {
        private final int statusCode;
        private final String responseBody;
        
        public IdempotencyResponse(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public String getResponseBody() {
            return responseBody;
        }
    }
}
