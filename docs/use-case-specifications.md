# Use Case Specifications - Narrative Form

## UC-001: Create Transaction

### Use Case Information
- **Use Case ID**: UC-001
- **Use Case Name**: Create Transaction
- **Primary Actor**: External System/User
- **Secondary Actors**: None
- **Preconditions**: 
  - Feature flag "create-transaction" is enabled
  - System is operational and database is accessible
- **Postconditions**: 
  - New transaction is created and persisted
  - Transaction is assigned a unique identifier
  - Transaction creation metric is recorded

### Basic Flow
1. **Actor initiates transaction creation**: The actor sends a POST request to `/transactions` endpoint with transaction data including amount, description, and type (EXPENSE or REVENUE).

2. **System validates feature flag**: The system checks if the "create-transaction" feature flag is enabled. If enabled, processing continues to step 3.

3. **System tracks metrics**: The system records a "transactions.created" metric for monitoring purposes.

4. **System validates input data**: The system validates the provided transaction data:
   - Amount must be greater than zero
   - Description must not be null or empty
   - Transaction type must be either EXPENSE or REVENUE

5. **System creates domain object**: The system creates a new Transaction domain object with:
   - Generated UUID as identifier
   - Current timestamp as creation date
   - Validated amount, description, and type

6. **System persists transaction**: The system saves the transaction to the database through the repository layer.

7. **System returns success response**: The system returns HTTP 200 with the created transaction object including the assigned ID and timestamp.

### Alternative Flows

#### A1: Feature Flag Disabled
- **Trigger**: Feature flag "create-transaction" is disabled
- **Steps**:
  1. System checks feature flag and determines it is disabled
  2. System throws FeatureFlagDisabledException
  3. Global exception handler catches the exception
  4. System returns HTTP 403 Forbidden with error message: "Feature 'create-transaction' is disabled"
- **Postcondition**: No transaction is created

#### A2: Invalid Amount
- **Trigger**: Amount is zero or negative
- **Steps**:
  1. System validates amount and determines it is invalid
  2. System throws IllegalArgumentException with message "Amount must be positive"
  3. Global exception handler catches the exception
  4. System returns HTTP 400 Bad Request with error message
- **Postcondition**: No transaction is created

#### A3: Empty Description
- **Trigger**: Description is null or empty
- **Steps**:
  1. System validates description and determines it is invalid
  2. System throws IllegalArgumentException with message "Description must not be null or empty"
  3. Global exception handler catches the exception
  4. System returns HTTP 400 Bad Request with error message
- **Postcondition**: No transaction is created

#### A4: Database Error
- **Trigger**: Database is unavailable or connection fails
- **Steps**:
  1. System attempts to persist transaction
  2. Database operation fails
  3. System throws database exception
  4. Global exception handler catches the exception
  5. System returns HTTP 500 Internal Server Error with generic error message
- **Postcondition**: No transaction is created

### Exception Flows
- **System Unavailable**: If the application server is down, the request will fail with connection error
- **Malformed Request**: If the request body is not valid JSON, the system returns HTTP 400 Bad Request

---

## UC-002: Retrieve All Transactions

### Use Case Information
- **Use Case ID**: UC-002
- **Use Case Name**: Retrieve All Transactions
- **Primary Actor**: External System/User
- **Secondary Actors**: None
- **Preconditions**: 
  - Feature flag "get-all-transactions" is enabled
  - System is operational and database is accessible
- **Postconditions**: 
  - All transactions are retrieved and returned
  - Transaction retrieval metric is recorded

### Basic Flow
1. **Actor requests all transactions**: The actor sends a GET request to `/transactions` endpoint.

2. **System validates feature flag**: The system checks if the "get-all-transactions" feature flag is enabled. If enabled, processing continues to step 3.

3. **System tracks metrics**: The system records a "transactions.fetched" metric for monitoring purposes.

4. **System retrieves transactions**: The system queries the database to retrieve all stored transactions.

5. **System maps entities to domain objects**: The system converts database entities to domain objects for each retrieved transaction.

6. **System returns transaction list**: The system returns HTTP 200 with a list of all transactions, or an empty list if no transactions exist.

### Alternative Flows

#### A1: Feature Flag Disabled
- **Trigger**: Feature flag "get-all-transactions" is disabled
- **Steps**:
  1. System checks feature flag and determines it is disabled
  2. System throws FeatureFlagDisabledException
  3. Global exception handler catches the exception
  4. System returns HTTP 403 Forbidden with error message: "Feature 'get-all-transactions' is disabled"
- **Postcondition**: No transactions are returned

#### A2: Database Error
- **Trigger**: Database is unavailable or query fails
- **Steps**:
  1. System attempts to query database
  2. Database operation fails
  3. System throws database exception
  4. Global exception handler catches the exception
  5. System returns HTTP 500 Internal Server Error with generic error message
- **Postcondition**: No transactions are returned

#### A3: No Transactions Found
- **Trigger**: Database contains no transactions
- **Steps**:
  1. System queries database
  2. Database returns empty result set
  3. System creates empty list
  4. System returns HTTP 200 with empty list: []
- **Postcondition**: Empty list is returned (this is considered normal operation)

### Exception Flows
- **System Unavailable**: If the application server is down, the request will fail with connection error

---

## UC-003: System Health Check

### Use Case Information
- **Use Case ID**: UC-003
- **Use Case Name**: System Health Check
- **Primary Actor**: Monitoring System
- **Secondary Actors**: None
- **Preconditions**: 
  - Application is running
  - Actuator endpoints are enabled
- **Postconditions**: 
  - System health status is determined and returned
  - Health check metric is recorded (if configured)

### Basic Flow
1. **Monitoring system requests health status**: The monitoring system sends a GET request to `/actuator/health` endpoint.

2. **System performs health checks**: The system executes various health checks:
   - Application status verification
   - Database connectivity test
   - Memory and disk space checks (if configured)

3. **System determines overall health**: The system aggregates results from all health checks to determine overall system status.

4. **System returns health status**: The system returns HTTP 200 with health status information including:
   - Overall status (UP/DOWN)
   - Individual component statuses
   - Additional health details (if configured)

### Alternative Flows

#### A1: Database Unavailable
- **Trigger**: Database connection fails
- **Steps**:
  1. System attempts database connectivity test
  2. Database connection fails
  3. System marks database component as DOWN
  4. System determines overall status as DOWN
  5. System returns HTTP 503 Service Unavailable with health details
- **Postcondition**: System is marked as unhealthy

#### A2: Application Issues
- **Trigger**: Application has internal issues
- **Steps**:
  1. System performs application health checks
  2. Application health check fails
  3. System marks application as DOWN
  4. System determines overall status as DOWN
  5. System returns HTTP 503 Service Unavailable with health details
- **Postcondition**: System is marked as unhealthy

### Exception Flows
- **Actuator Disabled**: If actuator endpoints are disabled, the request will return HTTP 404 Not Found

---

## UC-004: View API Documentation

### Use Case Information
- **Use Case ID**: UC-004
- **Use Case Name**: View API Documentation
- **Primary Actor**: Developer/User
- **Secondary Actors**: None
- **Preconditions**: 
  - Application is running
  - SpringDoc OpenAPI is enabled
- **Postconditions**: 
  - API documentation is displayed
  - User can interact with API endpoints

### Basic Flow
1. **User accesses API documentation**: The user navigates to `/swagger-ui.html` in a web browser.

2. **System serves documentation**: The system serves the Swagger UI interface with complete API documentation.

3. **User views API endpoints**: The user can view all available endpoints, request/response schemas, and example data.

4. **User can test endpoints**: The user can interact with the API directly through the Swagger UI interface.

### Alternative Flows

#### A1: Documentation Disabled
- **Trigger**: SpringDoc OpenAPI is disabled in configuration
- **Steps**:
  1. User attempts to access documentation URL
  2. System returns HTTP 404 Not Found
- **Postcondition**: Documentation is not accessible

### Exception Flows
- **System Unavailable**: If the application server is down, the documentation will not be accessible

---

## UC-005: Handle Invalid Request

### Use Case Information
- **Use Case ID**: UC-005
- **Use Case Name**: Handle Invalid Request
- **Primary Actor**: External System/User
- **Secondary Actors**: None
- **Preconditions**: 
  - System is operational
- **Postconditions**: 
  - Invalid request is properly rejected
  - Appropriate error response is returned

### Basic Flow
1. **Actor sends invalid request**: The actor sends a request with invalid data (malformed JSON, missing required fields, etc.).

2. **System validates request**: The system attempts to parse and validate the incoming request.

3. **System detects validation errors**: The system identifies specific validation failures.

4. **System returns error response**: The system returns HTTP 400 Bad Request with detailed error information about what was invalid.

### Alternative Flows

#### A1: Malformed JSON
- **Trigger**: Request body contains invalid JSON syntax
- **Steps**:
  1. System attempts to parse JSON
  2. JSON parsing fails
  3. System returns HTTP 400 Bad Request with message about JSON syntax error
- **Postcondition**: Request is rejected

#### A2: Missing Required Fields
- **Trigger**: Request is missing required fields
- **Steps**:
  1. System validates request structure
  2. System identifies missing required fields
  3. System returns HTTP 400 Bad Request with message listing missing fields
- **Postcondition**: Request is rejected

#### A3: Invalid Field Types
- **Trigger**: Request contains fields with incorrect data types
- **Steps**:
  1. System validates field types
  2. System identifies type mismatches
  3. System returns HTTP 400 Bad Request with message about type errors
- **Postcondition**: Request is rejected

### Exception Flows
- **System Unavailable**: If the application server is down, the request will fail with connection error

---

## Cross-Cutting Concerns

### Metrics Collection
All use cases automatically collect metrics through AOP aspects:
- **UC-001**: `transactions.created` counter
- **UC-002**: `transactions.fetched` counter
- **UC-003**: `health.checks` counter (if configured)
- **All**: `http.server.requests` metrics (Spring Boot default)

### Feature Flag Management
- **UC-001**: Controlled by `feature.create-transaction.enabled`
- **UC-002**: Controlled by `feature.get-all-transactions.enabled`
- **UC-003**: No feature flag (always available)
- **UC-004**: No feature flag (always available)
- **UC-005**: No feature flag (always available)

### Error Handling
All use cases follow consistent error handling patterns:
- **Validation Errors**: HTTP 400 Bad Request
- **Feature Flag Errors**: HTTP 403 Forbidden
- **System Errors**: HTTP 500 Internal Server Error
- **Not Found**: HTTP 404 Not Found
- **Service Unavailable**: HTTP 503 Service Unavailable

### Security Considerations
- All endpoints are currently unsecured (no authentication/authorization)
- Input validation prevents injection attacks
- Error messages don't expose sensitive system information
- Feature flags provide runtime access control
