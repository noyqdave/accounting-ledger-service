package com.example.ledger.config;

import java.util.Optional;

/**
 * Service for handling idempotency key operations.
 * 
 * This interface will be implemented to:
 * - Check if an idempotency key exists and has a cached response
 * - Store responses for idempotency keys
 * - Validate idempotency keys
 * 
 * TODO: Implement this service as part of Increment 1 of idempotency implementation.
 * See docs/idempotency-user-stories.md for implementation plan.
 */
public interface IdempotencyService {
    
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

