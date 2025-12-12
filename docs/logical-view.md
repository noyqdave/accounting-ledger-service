# Logical View - System Decomposition

![Logical View Diagram](diagrams/logical-view.mmd)

## Key Abstractions

### Domain Layer
- **Transaction**: Core business entity with validation rules
- **TransactionType**: Enum defining EXPENSE and REVENUE types
- **TransactionRepositoryPort**: Interface defining persistence contract

### Application Layer
- **Use Cases**: Business operations (Create, GetAll)
- **Services**: Implementation of business logic
- **Ports**: Interfaces defining contracts for external systems
  - **TransactionRepositoryPort**: Repository contract for transaction persistence
  - **IdempotencyRepositoryPort**: Interface defining idempotency key storage contract
    - `getCachedResponse()`: Retrieve cached response for idempotency key
    - `storeResponse()`: Store response with idempotency key and TTL
    - `isValidKey()`: Validate idempotency key format (UUID)
    - `hasKeyWithDifferentHash()`: Check for conflicts (same key, different request)
    - `deleteExpiredKeys()`: Delete expired idempotency keys (used by scheduler)

### Infrastructure Layer
- **Controllers**: REST API endpoints
- **IdempotencyFilter**: HTTP filter for idempotency key processing (inbound adapter)
- **Adapters**: Port implementations for external systems
  - **TransactionRepositoryAdapter**: Database persistence adapter (outbound)
  - **DatabaseIdempotencyAdapter**: Database-backed idempotency storage (outbound)
  - **InMemoryIdempotencyAdapter**: In-memory idempotency key storage adapter (outbound)
  - **IdempotencyCleanupScheduler**: Scheduled task for cleaning expired keys (outbound)
- **Entities**: Database persistence objects
  - **TransactionEntity**: JPA entity for transactions
  - **IdempotencyEntity**: JPA entity for idempotency keys

### Configuration Layer
- **FeatureFlagService**: Interface for feature flag checking
- **FeatureFlagServiceImpl**: Implementation reading from application.yml
- **FeatureFlagFilter**: HTTP filter for feature flag enforcement
- **MetricsAspect**: AOP aspect for metrics collection
- **GlobalExceptionHandler**: Centralized error handling
