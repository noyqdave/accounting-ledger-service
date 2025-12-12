# Process View - Runtime Behavior

## Transaction Creation Flow

![Transaction Creation Flow](diagrams/process-view-creation.mmd)

## Transaction Retrieval Flow

![Transaction Retrieval Flow](diagrams/process-view-retrieval.mmd)

## Feature Flag Disabled Flow

When a feature flag is disabled, the FeatureFlagFilter intercepts the request before it reaches the controller and returns a 403 Forbidden response with a JSON error message.

## Key Runtime Behaviors

### Request Processing Pipeline
1. **Idempotency Processing** (if Idempotency-Key header present): IdempotencyFilter checks for cached responses or conflicts before processing
2. **Feature Flag Validation**: HTTP filter (FeatureFlagFilter) checks feature configuration via FeatureFlagService
3. **Request Routing**: If enabled, request proceeds to controller; if disabled, returns 403
4. **Metrics Collection**: AOP aspect tracks operation metrics
5. **Input Validation**: Domain model validates business rules
6. **Business Logic**: Application services execute use cases
7. **Data Persistence**: Repository adapters handle database operations
8. **Response Caching** (if idempotency key present): Response is cached with expiration time (default 24 hours) for future retry requests
9. **Response Mapping**: Domain objects returned to clients

### Background Processing
- **Scheduled Cleanup**: IdempotencyCleanupScheduler runs every hour to delete expired idempotency keys
  - Calls `IdempotencyRepositoryPort.deleteExpiredKeys()`
  - Logs cleanup operations with duration
  - Handles errors gracefully without affecting normal operations

### Error Handling
- **Global Exception Handler**: Centralized error processing
- **Idempotency Conflicts**: IdempotencyFilter detects conflicts (same key, different request) and returns 409 Conflict
- **Feature Flag Exceptions**: FeatureFlagFilter handles disabled features at HTTP layer, returns 403 with JSON error
- **Validation Errors**: Input validation with meaningful error messages
- **Database Errors**: JPA exception translation
