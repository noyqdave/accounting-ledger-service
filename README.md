# Accounting Ledger Service

A Spring Boot microservice for managing financial transactions (expenses and revenue) built using Hexagonal Architecture (Ports and Adapters) pattern.

## Overview

The Accounting Ledger Service provides a RESTful API for creating and retrieving financial transactions. It features:
- Feature flag support for runtime feature control
- Metrics tracking and observability
- Comprehensive BDD testing with Cucumber
- Clean architecture with separation of concerns

## Features

- **Transaction Management**: Create and retrieve financial transactions (expenses and revenue)
- **Idempotency Support**: Safe retry handling with idempotency keys to prevent duplicate transactions
- **Feature Flags**: Runtime control of features via configuration
- **Metrics & Observability**: Built-in metrics collection and health checks
- **API Documentation**: OpenAPI/Swagger documentation
- **BDD Testing**: Behavior-driven development with Cucumber
- **Scheduled Tasks**: Automatic cleanup of expired idempotency keys

## Architecture

This service follows **Hexagonal Architecture** (Ports and Adapters) pattern:

- **Domain Layer**: Core business logic and entities
- **Application Layer**: Use cases and business services
- **Infrastructure Layer**: REST controllers and persistence adapters
- **Configuration Layer**: Cross-cutting concerns (feature flags, metrics, exception handling)

For detailed architecture documentation, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Technology Stack

- **Framework**: Spring Boot 3.2.8
- **Language**: Java 17
- **Database**: H2 (in-memory)
- **Persistence**: Spring Data JPA
- **API Documentation**: SpringDoc OpenAPI 2.3.0
- **Metrics**: Micrometer
- **Testing**: JUnit, Cucumber, RestAssured
- **Build Tool**: Maven

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Building the Project

```bash
# Compile the project
mvn clean compile

# Run tests
mvn test

# Run BDD tests
mvn test -Dtest=CucumberTestRunner

# Package the application
mvn clean package
```

### Running the Application

```bash
# Run the Spring Boot application
mvn spring-boot:run

# Or run the JAR file
java -jar target/ledger-service-1.0-SNAPSHOT.jar
```

The service will start on `http://localhost:8080`

### API Documentation

Once the application is running, access the API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Health Check

Check the service health status:
```bash
curl http://localhost:8080/actuator/health
```

## API Endpoints

### Create Transaction
```http
POST /transactions
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json

{
  "amount": 100.50,
  "description": "Office supplies",
  "type": "EXPENSE"
}
```

**Idempotency Key** (optional): Include an `Idempotency-Key` header with a UUID to safely retry requests. If the same key and request are sent again, the original response is returned without creating a duplicate transaction.

### Get All Transactions
```http
GET /transactions
```

## Configuration

Feature flags can be configured in `application.yml`:

```yaml
feature-flags:
  create-transaction: true
  get-all-transactions: true
```

## Testing

The project includes comprehensive testing:

- **Unit Tests**: Domain model and business logic validation
- **Integration Tests**: Controller and repository testing
- **BDD Tests**: Cucumber feature files with step definitions

Run all tests:
```bash
mvn test
```

Run BDD tests:
```bash
mvn test -Dtest=CucumberTestRunner
```

## Project Structure

```
src/
├── main/java/com/example/ledger/
│   ├── adapters/              # Infrastructure adapters
│   │   ├── in/web/           # REST controllers and filters
│   │   └── out/              # Outbound adapters
│   │       ├── persistence/  # Database adapters
│   │       ├── idempotency/  # Idempotency storage adapters
│   │       └── scheduling/   # Scheduled task adapters
│   ├── application/          # Application layer
│   │   ├── usecase/          # Application services
│   │   └── port/             # Application ports
│   ├── domain/               # Domain models and ports
│   └── config/               # Configuration and cross-cutting concerns
└── test/
    ├── java/                 # Test classes
    └── resources/features/   # Cucumber feature files
```

## Documentation

- [Architecture Documentation](ARCHITECTURE.md)
- [Use Case Specifications](docs/use-case-specifications.md)
- [User Stories](docs/user-stories.md)
- [BDD Scenarios](docs/bdd-scenarios.md)

## License

See [LICENSE](LICENSE) file for details.
