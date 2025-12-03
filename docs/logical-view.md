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

### Infrastructure Layer
- **Controllers**: REST API endpoints
- **Adapters**: Port implementations for external systems
- **Entities**: Database persistence objects

### Configuration Layer
- **FeatureFlagService**: Interface for feature flag checking
- **FeatureFlagServiceImpl**: Implementation reading from application.yml
- **FeatureFlagFilter**: HTTP filter for feature flag enforcement
- **MetricsAspect**: AOP aspect for metrics collection
- **GlobalExceptionHandler**: Centralized error handling
