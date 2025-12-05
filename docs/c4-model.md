# C4 Model - Accounting Ledger Service

The C4 model provides a hierarchical set of software architecture diagrams for describing the static structure of a software system. This document presents the Accounting Ledger Service using the C4 model at three levels of abstraction.

## Overview

The C4 model consists of four diagram types, each providing a different perspective on the software architecture:

1. **Context Diagram** - Shows the system and its relationships with users and other systems
2. **Container Diagram** - Shows the applications and data stores within the system
3. **Component Diagram** - Shows the components within a container
4. **Code Diagram** - Shows classes within a component (typically done in IDE)

## 1. Context Diagram

The context diagram shows the Accounting Ledger Service system and its relationships with users and external systems.

![Context Diagram](diagrams/c4-context.mmd)

### Actors

- **External System/User**: External applications or users that interact with the ledger service to create and retrieve financial transactions

### System

- **Accounting Ledger Service**: A microservice that manages financial transactions (expenses and revenue) with features like feature flags, metrics tracking, and comprehensive testing

### Relationships

- External System/User **creates and retrieves transactions** via REST API → Accounting Ledger Service

## 2. Container Diagram

The container diagram shows the high-level technical building blocks of the Accounting Ledger Service.

![Container Diagram](diagrams/c4-container.mmd)

### Containers

1. **Web Application** (Spring Boot Application)
   - **Technology**: Spring Boot 3.2.8, Java 17
   - **Responsibilities**: 
     - Provides REST API endpoints for transaction management
     - Handles HTTP requests and responses
     - Implements feature flag filtering
     - Collects metrics and observability data
   - **Port**: 8080

2. **Database** (H2 In-Memory Database)
   - **Technology**: H2 Database, Spring Data JPA
   - **Responsibilities**:
     - Stores transaction data persistently
     - Provides data persistence layer via JPA

### Relationships

- External System/User **sends HTTP requests** → Web Application
- Web Application **reads and writes data** → Database
- Web Application **exposes metrics** → External Monitoring Systems (via Actuator endpoints)

## 3. Component Diagram

The component diagram shows the components within the Web Application container and how they interact.

![Component Diagram](diagrams/c4-component.mmd)

### Components

#### Inbound Adapters (Web Layer)

1. **TransactionController**
   - **Type**: REST Controller
   - **Responsibilities**:
     - Exposes REST endpoints (`POST /transactions`, `GET /transactions`)
     - Handles HTTP request/response mapping
     - Converts DTOs to domain models
   - **Technology**: Spring Web MVC

2. **FeatureFlagFilter**
   - **Type**: HTTP Filter
   - **Responsibilities**:
     - Intercepts HTTP requests
     - Validates feature flags before processing
     - Returns 403 Forbidden if feature is disabled
   - **Technology**: Spring Servlet Filter

#### Application Layer

3. **CreateTransactionService**
   - **Type**: Application Service
   - **Responsibilities**:
     - Implements transaction creation business logic
     - Validates transaction data
     - Coordinates with repository for persistence
   - **Technology**: Plain Java

4. **GetAllTransactionsService**
   - **Type**: Application Service
   - **Responsibilities**:
     - Implements transaction retrieval business logic
     - Retrieves all transactions from repository
   - **Technology**: Plain Java

#### Domain Layer

5. **Transaction** (Domain Model)
   - **Type**: Domain Entity
   - **Responsibilities**:
     - Represents core business entity
     - Contains business validation rules
     - Encapsulates transaction data and behavior
   - **Technology**: Plain Java

#### Outbound Adapters (Persistence Layer)

6. **TransactionRepositoryAdapter**
   - **Type**: Repository Adapter
   - **Responsibilities**:
     - Implements TransactionRepositoryPort interface
     - Maps domain models to database entities
     - Provides abstraction over database operations
   - **Technology**: Spring Data JPA

7. **TransactionJpaRepository**
   - **Type**: JPA Repository
   - **Responsibilities**:
     - Provides database CRUD operations
     - Handles JPA entity persistence
   - **Technology**: Spring Data JPA

#### Cross-Cutting Concerns

8. **FeatureFlagService**
   - **Type**: Configuration Service
   - **Responsibilities**:
     - Reads feature flag configuration
     - Provides feature flag status checking
   - **Technology**: Spring Configuration

9. **MetricsAspect**
   - **Type**: AOP Aspect
   - **Responsibilities**:
     - Intercepts method calls annotated with @TrackMetric
     - Collects and records metrics
   - **Technology**: Spring AOP, Micrometer

10. **GlobalExceptionHandler**
    - **Type**: Exception Handler
    - **Responsibilities**:
      - Handles exceptions globally
      - Converts exceptions to appropriate HTTP responses
    - **Technology**: Spring Web MVC

### Relationships

- External System/User **sends HTTP requests** → FeatureFlagFilter
- FeatureFlagFilter **validates flags** → FeatureFlagService
- FeatureFlagFilter **forwards requests** → TransactionController
- TransactionController **uses** → CreateTransactionService, GetAllTransactionsService
- CreateTransactionService **creates** → Transaction (domain model)
- GetAllTransactionsService **retrieves** → Transaction (domain model)
- CreateTransactionService, GetAllTransactionsService **persist/retrieve** → TransactionRepositoryAdapter
- TransactionRepositoryAdapter **uses** → TransactionJpaRepository
- TransactionRepositoryAdapter **maps** → TransactionEntity
- MetricsAspect **intercepts** → TransactionController methods
- GlobalExceptionHandler **handles exceptions** → TransactionController

## Architecture Patterns

### Hexagonal Architecture (Ports and Adapters)

The system follows Hexagonal Architecture principles:

- **Domain Layer**: Core business logic (Transaction, TransactionType)
- **Application Layer**: Use cases and business services
- **Ports**: Interfaces defining contracts (TransactionRepositoryPort)
- **Adapters**: 
  - **Inbound Adapters**: TransactionController, FeatureFlagFilter
  - **Outbound Adapters**: TransactionRepositoryAdapter

### Cross-Cutting Concerns

- **Aspect-Oriented Programming**: MetricsAspect for metrics collection
- **HTTP Filter Pattern**: FeatureFlagFilter for feature flag enforcement
- **Global Exception Handling**: GlobalExceptionHandler for consistent error responses

## Technology Stack

- **Framework**: Spring Boot 3.2.8
- **Language**: Java 17
- **Database**: H2 (in-memory, development)
- **Persistence**: Spring Data JPA
- **API Documentation**: SpringDoc OpenAPI 2.3.0
- **Metrics**: Micrometer
- **AOP**: Spring AOP

## Notes

- The **Code Diagram** (Level 4) is typically created in IDEs and shows the classes and their relationships within each component. This level of detail is usually maintained in the codebase itself rather than in documentation.
- The database shown is H2 for development. In production, this would typically be replaced with PostgreSQL, MySQL, or another production-grade database.
- The system is designed to be stateless and horizontally scalable, with feature flags providing runtime control over functionality.
