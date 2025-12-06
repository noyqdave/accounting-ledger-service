# Accounting Ledger Service - 4+1 Architectural View

## Overview

The Accounting Ledger Service is a Java microservice built using **Hexagonal Architecture** (Ports and Adapters) pattern with Spring Boot. It provides functionality for managing financial transactions (expenses and revenue) with features like feature flags, metrics tracking, and comprehensive testing.

> **Note**: This document uses the 4+1 architectural view model. For a C4 model perspective, see [C4 Model Documentation](docs/c4-model.md).

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
- **IdempotencyRepositoryPort**: Interface defining idempotency key storage contract

### Infrastructure Layer
- **TransactionController**: REST API controller (Inbound Adapter)
- **IdempotencyFilter**: HTTP filter for idempotency key processing (Inbound Adapter)
- **TransactionRepositoryAdapter**: JPA repository adapter (Outbound Adapter)
- **InMemoryIdempotencyAdapter**: In-memory idempotency key storage adapter (Outbound Adapter)
- **TransactionEntity**: JPA entity for database persistence
- **TransactionJpaRepository**: Spring Data JPA repository

### Configuration Layer
- **FeatureFlagService**: Interface for feature flag checking
- **FeatureFlagServiceImpl**: Implementation reading from application properties
- **FeatureFlagFilter**: HTTP filter for feature flag enforcement at request level
- **MetricsAspect**: AOP aspect for metrics collection
- **GlobalExceptionHandler**: Global exception handling

## 2. Process View

The process view illustrates the runtime behavior and interactions between components.

### Transaction Creation Flow
1. **HTTP Request** → IdempotencyFilter intercepts request (if Idempotency-Key header present)
2. **Idempotency Check** (if key present) → IdempotencyFilter checks for cached response or conflicts via IdempotencyRepositoryPort
3. **Cached Response** (if found) → Return cached response immediately, flow ends
4. **Conflict Detection** (if same key, different request) → Return 409 Conflict, flow ends
5. **Feature Flag Check** → FeatureFlagFilter validates "create-transaction" flag via FeatureFlagService
6. **Request Forwarding** → If enabled, request proceeds to TransactionController
7. **Metrics Tracking** → MetricsAspect tracks "transactions.created" metric
8. **DTO Conversion** → CreateTransactionRequest → Transaction domain model
9. **Use Case Execution** → CreateTransactionService.create()
10. **Repository Call** → TransactionRepositoryAdapter.save()
11. **Entity Mapping** → Transaction → TransactionEntity
12. **Database Persistence** → TransactionJpaRepository.save()
13. **Response Mapping** → TransactionEntity → Transaction
14. **Response Caching** (if idempotency key present) → IdempotencyFilter caches response via IdempotencyRepositoryPort
15. **HTTP Response** → Transaction object returned

### Transaction Retrieval Flow
1. **HTTP Request** → FeatureFlagFilter intercepts request
2. **Feature Flag Check** → FeatureFlagFilter validates "get-all-transactions" flag via FeatureFlagService
3. **Request Forwarding** → If enabled, request proceeds to TransactionController
4. **Metrics Tracking** → MetricsAspect tracks "transactions.fetched" metric
5. **Use Case Execution** → GetAllTransactionsService.getAll()
6. **Repository Call** → TransactionRepositoryAdapter.findAll()
7. **Database Query** → TransactionJpaRepository.findAll()
8. **Entity Mapping** → List<TransactionEntity> → List<Transaction>
9. **HTTP Response** → List<Transaction> returned

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
    ├── FeatureFlagService.java
    ├── FeatureFlagServiceImpl.java
    ├── FeatureFlagFilter.java
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
2. FeatureFlagFilter intercepts request and validates "create-transaction" flag
3. If enabled, request proceeds to TransactionController
4. System tracks metric "transactions.created"
5. System validates transaction data (amount > 0, description not empty)
6. System creates Transaction domain object with generated UUID and timestamp
7. System persists transaction to database
8. System returns created transaction with assigned ID

**Success Criteria**: Transaction is created and persisted with valid ID

### Scenario 2: Retrieve All Transactions

**Actor**: External System/User
**Goal**: Get list of all transactions

**Steps**:
1. Send GET request to `/transactions`
2. FeatureFlagFilter intercepts request and validates "get-all-transactions" flag
3. If enabled, request proceeds to TransactionController
4. System tracks metric "transactions.fetched"
5. System retrieves all transactions from database
6. System maps database entities to domain objects
7. System returns list of transactions

**Success Criteria**: All transactions are returned in correct format

### Scenario 3: Feature Flag Disabled

**Actor**: External System/User
**Goal**: Attempt to use disabled feature

**Steps**:
1. Send request to endpoint with disabled feature flag
2. FeatureFlagFilter intercepts request and checks feature flag via FeatureFlagService
3. FeatureFlagService throws FeatureFlagDisabledException
4. FeatureFlagFilter catches exception and returns HTTP 403 Forbidden response with JSON error

**Success Criteria**: Request is properly rejected with appropriate error

## Architecture Patterns

### Hexagonal Architecture (Ports and Adapters)
- **Domain Core**: Pure business logic with no external dependencies
- **Ports**: Interfaces defining contracts (TransactionRepositoryPort)
- **Adapters**: Implementations of ports (TransactionRepositoryAdapter, TransactionController)

### Cross-Cutting Concerns
- **HTTP Filter Pattern**: Feature flags enforced via servlet filter (FeatureFlagFilter)
- **Aspect-Oriented Programming**: Metrics collection via Spring AOP (MetricsAspect)
- **Configuration Management**: Externalized configuration via application.yml
- **Exception Handling**: Global exception handler for consistent error responses

### Testing Strategy
- **Unit Tests**: Domain model validation and business logic
- **Integration Tests**: Controller endpoints and repository interactions
- **Feature Flag Tests**: Filter behavior validation with configurable service
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
