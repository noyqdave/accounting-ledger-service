# Idempotency Implementation Guide

## Overview

Currently, the transaction creation endpoint is **not idempotent**. If a client retries a request (e.g., due to network timeout), it will create duplicate transactions. This document outlines what needs to be implemented to add idempotency support.

## What is Idempotency?

Idempotency ensures that making the same request multiple times has the same effect as making it once. For transaction creation:
- **First request**: Creates transaction, returns response
- **Subsequent requests with same idempotency key**: Returns the same response without creating a duplicate transaction

## Required Components

### 1. Idempotency Key Storage

**Need**: A way to store idempotency keys and their associated responses.

**Options**:
- **Database Table** (Recommended for production):
  - Table: `idempotency_keys`
  - Columns: `id`, `idempotency_key`, `request_hash`, `response_body`, `status_code`, `created_at`, `expires_at`
  - Index on `idempotency_key` for fast lookups
  - TTL/expiration to clean up old keys (e.g., 24 hours)

- **In-Memory Cache** (Good for testing):
  - Use Spring's `@Cacheable` with Caffeine or Redis
  - Simpler setup but doesn't persist across restarts

### 2. Idempotency Key Header

**Need**: Accept an idempotency key from clients.

**Standard Approach**:
- Header: `Idempotency-Key: <unique-key>`
- Client generates a unique key (e.g., UUID) per logical transaction
- Client sends the same key on retries

**Alternative**:
- Include in request body (less standard, not recommended)

### 3. Idempotency Check Interceptor/Filter

**Need**: Middleware to check idempotency before processing.

**Implementation Strategy**:
- **Option A: HTTP Filter** (Similar to FeatureFlagFilter)
  - Check idempotency key header
  - If exists and valid → return cached response
  - If exists but request differs → return 409 Conflict
  - If doesn't exist → continue processing

- **Option B: Controller Interceptor**
  - Use Spring's `HandlerInterceptor`
  - Similar logic but later in the request pipeline

**Location**: `com.example.ledger.config.IdempotencyFilter`

### 4. Request Matching Logic

**Need**: Verify that a request with an existing idempotency key matches the original request.

**Approach**:
- Hash the request body (amount, description, type)
- Store hash with idempotency key
- On retry, compare request hash
- If different → return 409 Conflict (request conflict)
- If same → return cached response

### 5. Response Caching

**Need**: Store the response for successful requests.

**Storage**:
- Store HTTP status code
- Store response body (JSON)
- Store created transaction ID

**Retrieval**:
- Look up by idempotency key
- Return cached status code and body
- Should be identical to original response

## Implementation Steps

### Step 1: Create Idempotency Entity & Repository

```java
// entity/IdempotencyKeyEntity.java
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyEntity {
    @Id
    private String idempotencyKey;
    private String requestHash;
    private String responseBody;
    private int statusCode;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}

// IdempotencyKeyRepository.java
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {
    Optional<IdempotencyKeyEntity> findByIdempotencyKey(String key);
    void deleteByExpiresAtBefore(LocalDateTime now);
}
```

### Step 2: Create Idempotency Service

```java
// IdempotencyService.java
public interface IdempotencyService {
    Optional<IdempotencyResponse> getCachedResponse(String idempotencyKey, String requestHash);
    void storeResponse(String idempotencyKey, String requestHash, IdempotencyResponse response);
    boolean isValidKey(String idempotencyKey);
}
```

### Step 3: Create Idempotency Filter

```java
@Component
public class IdempotencyFilter extends OncePerRequestFilter {
    // Check idempotency key header
    // If exists: lookup cached response
    // If cached and request matches: return cached response
    // If cached but request differs: return 409
    // If not cached: continue, store response after processing
}
```

### Step 4: Update TransactionController

- Add logic to store response after successful creation
- Ensure idempotency key is passed through the flow

### Step 5: Add Configuration

```yaml
# application.yml
idempotency:
  enabled: true
  header-name: "Idempotency-Key"
  ttl-hours: 24
```

### Step 6: Update API Documentation

- Document the `Idempotency-Key` header
- Explain idempotency behavior
- Add examples to Swagger/OpenAPI

### Step 7: Add Tests

- Test successful idempotency (same request, same response)
- Test request conflict (same key, different request)
- Test expiration
- Test without idempotency key (should work normally)

## Database Migration

If using database storage, create migration:

```sql
CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    request_hash VARCHAR(64) NOT NULL,
    response_body TEXT NOT NULL,
    status_code INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    INDEX idx_expires_at (expires_at)
);
```

## Cleanup Strategy

- **Scheduled Task**: Periodically delete expired idempotency keys
- **On Read**: Check expiration, delete if expired
- **TTL**: Typically 24 hours (align with client retry windows)

## Error Responses

- **409 Conflict**: When idempotency key exists but request differs
  ```json
  {
    "error": "Idempotency key already used with different request parameters"
  }
  ```

- **400 Bad Request**: When idempotency key format is invalid

## Benefits

1. **Prevents Duplicates**: Clients can safely retry failed requests
2. **Network Resilience**: Handles timeouts and retries gracefully
3. **Financial Safety**: Critical for payment/transaction systems
4. **Better UX**: Clients get consistent responses

## Considerations

- **Storage Size**: Old keys should be cleaned up
- **Performance**: Lookup should be fast (use indexes)
- **Key Format**: Should validate key format (UUID recommended)
- **Key Length**: Limit key size to prevent abuse
- **Race Conditions**: Handle concurrent requests with same key (use database constraints)

## Example Client Usage

```http
POST /transactions
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json

{
  "amount": 100.00,
  "description": "Office supplies",
  "type": "EXPENSE"
}
```

If this request is retried with the same key and body, it will return the same response without creating a duplicate transaction.

---

## Feature File Organization

### Decision: Separate Feature Files for Idempotency

**Decision**: We will **not** update the existing `create-transaction.feature` file to include idempotency key examples. Instead, idempotency behavior is tested in a separate `idempotency.feature` file.

### Reasoning

1. **Separation of Concerns**:
   - `create-transaction.feature` focuses on transaction validation (amount, description, type validation)
   - `idempotency.feature` focuses specifically on idempotency behavior
   - Mixing concerns would make both files harder to understand

2. **Optional Header**:
   - The `Idempotency-Key` header is optional
   - Existing transaction creation scenarios should continue to work unchanged
   - No need to modify existing feature files when adding optional functionality

3. **Backward Compatibility Already Demonstrated**:
   - The idempotency feature file includes a scenario: "Request Without Idempotency Key Works Normally"
   - This scenario proves that requests without the header work exactly as before
   - Adding idempotency examples to `create-transaction.feature` would be redundant

4. **Clear Documentation**:
   - Each feature file documents a distinct capability
   - Developers looking for transaction validation tests know where to look
   - Developers looking for idempotency tests know where to look
   - Clearer intent and easier maintenance

### Implementation Approach

- **Step Definitions**: The `TransactionStepDefinitions` class supports the optional idempotency key header at the implementation level
- **Shared State**: Both step definition classes use a shared `TestContext` to coordinate when needed
- **Feature Files**: Each feature file remains focused on its specific concern
  - `create-transaction.feature` → Transaction validation
  - `idempotency.feature` → Idempotency behavior

This approach maintains clean separation while ensuring all functionality is properly tested.

