# Development View - Module Organization

## Package Structure

![Development View Diagram](diagrams/development-view.mmd)

## Dependency Flow

```mermaid
graph TB
    subgraph "Domain Layer"
        T[Transaction]
        TT[TransactionType]
        TRP[TransactionRepositoryPort]
    end
    
    subgraph "Application Layer"
        CTU[CreateTransactionUseCase]
        CTS[CreateTransactionService]
        GTU[GetAllTransactionsUseCase]
        GTS[GetAllTransactionsService]
    end
    
    subgraph "Infrastructure Layer"
        TC[TransactionController]
        TRA[TransactionRepositoryAdapter]
        TE[TransactionEntity]
    end
    
    subgraph "Configuration Layer"
        FF[FeatureFlags]
        FFA[FeatureFlagAspect]
        MA[MetricsAspect]
        GEH[GlobalExceptionHandler]
    end
    
    %% Dependencies (arrows point from dependent to dependency)
    CTS --> TRP
    GTS --> TRP
    TC --> CTU
    TC --> GTU
    TRA --> TRP
    TRA --> TE
    
    %% Cross-cutting (dotted lines)
    FFA -.-> TC
    MA -.-> TC
    GEH -.-> TC
```

## Module Responsibilities

### Domain Layer (`domain/`)
- **Purpose**: Core business logic and rules
- **Dependencies**: None (pure Java)
- **Key Classes**:
  - `Transaction`: Business entity with validation
  - `TransactionType`: Business enumeration
  - `TransactionRepositoryPort`: Repository interface

### Application Layer (`application/usecase/`)
- **Purpose**: Application services and use case orchestration
- **Dependencies**: Domain layer only
- **Key Classes**:
  - `CreateTransactionUseCase`: Interface for transaction creation
  - `CreateTransactionService`: Implementation of creation logic
  - `GetAllTransactionsUseCase`: Interface for transaction retrieval
  - `GetAllTransactionsService`: Implementation of retrieval logic

### Infrastructure Layer (`adapters/`)
- **Purpose**: External system integration and persistence
- **Dependencies**: Application and Domain layers
- **Key Classes**:
  - `TransactionController`: REST API controller
  - `TransactionRepositoryAdapter`: JPA repository implementation
  - `TransactionEntity`: Database entity mapping

### Configuration Layer (`config/`)
- **Purpose**: Cross-cutting concerns and system configuration
- **Dependencies**: Used by all layers
- **Key Classes**:
  - `FeatureFlags`: Feature flag configuration
  - `FeatureFlagAspect`: AOP for feature flag enforcement
  - `MetricsAspect`: AOP for metrics collection
  - `GlobalExceptionHandler`: Centralized error handling

## Build Configuration

### Maven Structure
```
pom.xml
src/
├── main/
│   ├── java/
│   │   └── com/example/ledger/
│   └── resources/
│       ├── application.yml
│       └── transaction.http
└── test/
    └── java/
        └── com/example/ledger/
```

### Key Dependencies
- **Spring Boot Starter Web**: REST API framework
- **Spring Boot Starter Data JPA**: Database persistence
- **SpringDoc OpenAPI**: API documentation
- **Micrometer**: Metrics collection
- **H2 Database**: In-memory database
- **Spring Boot Starter Test**: Testing framework

## Testing Strategy

### Test Organization
```
src/test/java/com/example/ledger/
├── adapters/in/web/
│   ├── TransactionControllerTest.java
│   ├── TransactionFeatureFlagDisabledTest.java
│   └── TransactionMetricsIntegrationTest.java
├── application/usecase/
│   ├── CreateTransactionServiceTest.java
│   └── GetAllTransactionsServiceTest.java
└── domain/model/
    └── TransactionTest.java
```

### Test Types
- **Unit Tests**: Domain model validation and business logic
- **Integration Tests**: Controller endpoints and repository interactions
- **Feature Flag Tests**: AOP behavior validation
- **Metrics Tests**: AOP metrics collection validation

## Development Guidelines

### Code Organization Principles
1. **Dependency Inversion**: High-level modules don't depend on low-level modules
2. **Interface Segregation**: Small, focused interfaces
3. **Single Responsibility**: Each class has one reason to change
4. **Open/Closed**: Open for extension, closed for modification

### Naming Conventions
- **Domain Models**: Business-focused names (Transaction, TransactionType)
- **Use Cases**: Action-oriented names (CreateTransaction, GetAllTransactions)
- **Adapters**: Technology-specific names (TransactionController, TransactionRepositoryAdapter)
- **Configuration**: Cross-cutting concern names (FeatureFlags, MetricsAspect)
