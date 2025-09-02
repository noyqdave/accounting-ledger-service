# Accounting Ledger Service - 4+1 Architectural View

## Overview

The Accounting Ledger Service is a Java microservice built using **Hexagonal Architecture** (Ports and Adapters) pattern with Spring Boot. It provides functionality for managing financial transactions (expenses and revenue) with features like feature flags, metrics tracking, and comprehensive testing.

## 1. Logical View

The logical view shows the system's decomposition into key abstractions and their relationships.

### Core Domain Layer
- **Transaction**: Core domain entity representing financial transactions
- **TransactionType**: Enum defining transaction types (EXPENSE, REVENUE)
- **TransactionRepositoryPort**: Interface defining persistence contract

### Application Layer
- **CreateTransactionUseCase**: Interface for transaction creation
- **CreateTransactionService**: Implementation of transaction creation logic
- **GetAllTransactionsUseCase**: Interface for retrieving all transactions
- **GetAllTransactionsService**: Implementation of transaction retrieval logic

### Infrastructure Layer
- **TransactionController**: REST API controller (Inbound Adapter)
- **TransactionRepositoryAdapter**: JPA repository adapter (Outbound Adapter)
- **TransactionEntity**: JPA entity for database persistence
- **TransactionJpaRepository**: Spring Data JPA repository

### Configuration Layer
- **FeatureFlags**: Feature flag configuration management
- **FeatureFlagAspect**: AOP aspect for feature flag enforcement
- **MetricsAspect**: AOP aspect for metrics collection
- **GlobalExceptionHandler**: Global exception handling

## 2. Process View

The process view illustrates the runtime behavior and interactions between components.

### Transaction Creation Flow
1. **HTTP Request** → TransactionController
2. **Feature Flag Check** → FeatureFlagAspect validates "create-transaction" flag
3. **Metrics Tracking** → MetricsAspect tracks "transactions.created" metric
4. **DTO Conversion** → CreateTransactionRequest → Transaction domain model
5. **Use Case Execution** → CreateTransactionService.create()
6. **Repository Call** → TransactionRepositoryAdapter.save()
7. **Entity Mapping** → Transaction → TransactionEntity
8. **Database Persistence** → TransactionJpaRepository.save()
9. **Response Mapping** → TransactionEntity → Transaction
10. **HTTP Response** → Transaction object returned

### Transaction Retrieval Flow
1. **HTTP Request** → TransactionController
2. **Feature Flag Check** → FeatureFlagAspect validates "get-all-transactions" flag
3. **Metrics Tracking** → MetricsAspect tracks "transactions.fetched" metric
4. **Use Case Execution** → GetAllTransactionsService.getAll()
5. **Repository Call** → TransactionRepositoryAdapter.findAll()
6. **Database Query** → TransactionJpaRepository.findAll()
7. **Entity Mapping** → List<TransactionEntity> → List<Transaction>
8. **HTTP Response** → List<Transaction> returned

## 3. Physical View

The physical view shows the deployment architecture and infrastructure components.

### Runtime Environment
- **JVM**: Java 17 runtime environment
- **Spring Boot Application**: Embedded Tomcat server
- **H2 Database**: In-memory database for development/testing
- **Actuator Endpoints**: Health checks and metrics exposure

### External Dependencies
- **Spring Boot Starter Web**: REST API framework
- **Spring Boot Starter Data JPA**: Database persistence
- **SpringDoc OpenAPI**: API documentation
- **Micrometer**: Metrics collection
- **H2 Database**: In-memory database

### Deployment Configuration
- **Port**: Default Spring Boot port (8080)
- **Database**: H2 in-memory (development)
- **Metrics**: Exposed via Actuator endpoints
- **API Documentation**: Available at `/swagger-ui.html`

## 4. Development View

The development view shows the module organization and dependencies.

### Package Structure
```
com.example.ledger/
├── LedgerServiceApplication.java          # Main application class
├── adapters/
│   ├── in/web/                           # Inbound adapters
│   │   ├── TransactionController.java     # REST controller
│   │   └── dto/                          # Data transfer objects
│   └── out/persistence/                  # Outbound adapters
│       ├── TransactionRepositoryAdapter.java
│       └── entity/
│           └── TransactionEntity.java     # JPA entity
├── application/usecase/                   # Application services
│   ├── CreateTransactionService.java
│   ├── CreateTransactionUseCase.java
│   ├── GetAllTransactionsService.java
│   └── GetAllTransactionsUseCase.java
├── domain/                               # Domain layer
│   ├── model/
│   │   ├── Transaction.java              # Domain entity
│   │   └── TransactionType.java          # Domain enum
│   └── port/
│       └── TransactionRepositoryPort.java # Repository interface
└── config/                               # Configuration
    ├── FeatureFlags.java
    ├── FeatureFlagAspect.java
    ├── MetricsAspect.java
    └── GlobalExceptionHandler.java
```

### Dependency Flow
- **Domain Layer**: No dependencies (pure business logic)
- **Application Layer**: Depends only on Domain layer
- **Infrastructure Layer**: Depends on Application and Domain layers
- **Configuration Layer**: Cross-cutting concerns, used by all layers

## 5. Use Case Scenarios

### Scenario 1: Create a New Expense Transaction

**Actor**: External System/User
**Goal**: Record a new expense transaction

**Steps**:
1. Send POST request to `/transactions` with expense data
2. System validates feature flag "create-transaction"
3. System tracks metric "transactions.created"
4. System validates transaction data (amount > 0, description not empty)
5. System creates Transaction domain object with generated UUID and timestamp
6. System persists transaction to database
7. System returns created transaction with assigned ID

**Success Criteria**: Transaction is created and persisted with valid ID

### Scenario 2: Retrieve All Transactions

**Actor**: External System/User
**Goal**: Get list of all transactions

**Steps**:
1. Send GET request to `/transactions`
2. System validates feature flag "get-all-transactions"
3. System tracks metric "transactions.fetched"
4. System retrieves all transactions from database
5. System maps database entities to domain objects
6. System returns list of transactions

**Success Criteria**: All transactions are returned in correct format

### Scenario 3: Feature Flag Disabled

**Actor**: External System/User
**Goal**: Attempt to use disabled feature

**Steps**:
1. Send request to endpoint with disabled feature flag
2. System checks feature flag configuration
3. System throws FeatureFlagDisabledException
4. System returns HTTP 403 Forbidden response

**Success Criteria**: Request is properly rejected with appropriate error

## Architecture Patterns

### Hexagonal Architecture (Ports and Adapters)
- **Domain Core**: Pure business logic with no external dependencies
- **Ports**: Interfaces defining contracts (TransactionRepositoryPort)
- **Adapters**: Implementations of ports (TransactionRepositoryAdapter, TransactionController)

### Cross-Cutting Concerns
- **Aspect-Oriented Programming**: Feature flags and metrics via Spring AOP
- **Configuration Management**: Externalized configuration via application.yml
- **Exception Handling**: Global exception handler for consistent error responses

### Testing Strategy
- **Unit Tests**: Domain model validation and business logic
- **Integration Tests**: Controller endpoints and repository interactions
- **Feature Flag Tests**: AOP behavior validation
- **Metrics Tests**: AOP metrics collection validation

## Technology Stack

- **Framework**: Spring Boot 3.2.8
- **Language**: Java 17
- **Database**: H2 (in-memory)
- **Persistence**: Spring Data JPA
- **API Documentation**: SpringDoc OpenAPI 2.3.0
- **Metrics**: Micrometer
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Build Tool**: Maven
- **Containerization**: Docker

## Quality Attributes

- **Maintainability**: Clean architecture with separation of concerns
- **Testability**: Comprehensive test coverage with isolated unit tests
- **Scalability**: Stateless design suitable for horizontal scaling
- **Observability**: Built-in metrics and health checks
- **Flexibility**: Feature flags for runtime behavior control
- **Reliability**: Input validation and error handling
