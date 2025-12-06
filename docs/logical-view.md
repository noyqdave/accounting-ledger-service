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
- **IdempotencyRepositoryPort**: Interface defining idempotency key storage contract

### Infrastructure Layer
- **Controllers**: REST API endpoints
- **IdempotencyFilter**: HTTP filter for idempotency key processing (inbound adapter)
- **Adapters**: Port implementations for external systems
  - **TransactionRepositoryAdapter**: Database persistence adapter
  - **InMemoryIdempotencyAdapter**: In-memory idempotency key storage adapter
- **Entities**: Database persistence objects

### Configuration Layer
- **FeatureFlagService**: Interface for feature flag checking
- **FeatureFlagServiceImpl**: Implementation reading from application.yml
- **FeatureFlagFilter**: HTTP filter for feature flag enforcement
- **MetricsAspect**: AOP aspect for metrics collection
- **GlobalExceptionHandler**: Centralized error handling
