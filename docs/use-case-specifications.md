# Use Case Specifications - Narrative Form

## UC-001: Create Transaction

### Use Case Information
- **Use Case ID**: UC-001
- **Use Case Name**: Create Transaction
- **Primary Actor**: External System/User
- **Secondary Actors**: None
- **Preconditions**: 
  - System is operational and database is accessible
- **Postconditions**: 
  - New transaction is created and persisted
  - Transaction is assigned a unique identifier
  - Transaction creation metric is recorded

### Basic Flow
1. **Actor initiates transaction creation**: The actor sends a POST request to `/transactions` endpoint with transaction data including amount, description, and type (EXPENSE or REVENUE). Optionally, the actor may include an `Idempotency-Key` header.

2. **System validates request format**: The system validates that the request body contains valid JSON.

3. **System validates feature flag**: The system checks if the "create-transaction" feature flag is enabled.

4. **System processes idempotency key (if present)**: If an `Idempotency-Key` header is present:
   - System validates the idempotency key format (must be a valid UUID)
   - System computes a hash of the request body
   - System checks if a cached response exists for this idempotency key and request hash
   - If cached response exists: System returns the cached response (HTTP 200) and flow ends
   - If idempotency key exists with different request hash: System returns HTTP 409 Conflict and flow ends
   - If idempotency key is new: System continues with transaction creation

5. **System tracks metrics**: The system records a "transactions.created" metric for monitoring purposes.

6. **System validates input data**: The system validates the provided transaction data:
   - Amount must be greater than zero
   - Description must not be null or empty
   - Transaction type must be either EXPENSE or REVENUE

7. **System creates domain object**: The system creates a new Transaction domain object with:
   - Generated UUID as identifier
   - Current timestamp as creation date
   - Validated amount, description, and type

8. **System persists transaction**: The system saves the transaction to the database through the repository layer.

9. **System caches response (if idempotency key present)**: If an idempotency key was provided and transaction creation was successful, the system caches the response (status code and body) associated with the idempotency key and request hash.

10. **System returns success response**: The system returns HTTP 200 with the created transaction object including the assigned ID and timestamp.

### Alternative Flows

#### A1: Feature Flag Disabled
- **Trigger**: In step 3, the "create-transaction" feature flag is disabled
- **Steps**:
  1. System checks feature flag and determines it is disabled
  2. System throws FeatureFlagDisabledException
  3. Global exception handler catches the exception
  4. System returns HTTP 403 Forbidden with error message: "Feature 'create-transaction' is disabled"
- **Postcondition**: No transaction is created

#### A2: Invalid Amount
- **Trigger**: In step 5, the amount is zero or negative
- **Steps**:
  1. System validates amount and determines it is invalid
  2. System throws IllegalArgumentException with message "Amount must be positive"
  3. Global exception handler catches the exception
  4. System returns HTTP 400 Bad Request with error message
- **Postcondition**: No transaction is created

#### A3: Empty Description
- **Trigger**: In step 5, the description is null or empty
- **Steps**:
  1. System validates description and determines it is invalid
  2. System throws IllegalArgumentException with message "Description must not be null or empty"
  3. Global exception handler catches the exception
  4. System returns HTTP 400 Bad Request with error message
- **Postcondition**: No transaction is created

#### A4: Database Error
- **Trigger**: In step 7, the database is unavailable or connection fails
- **Steps**:
  1. System attempts to persist transaction
  2. Database operation fails
  3. System throws database exception
  4. Global exception handler catches the exception
  5. System returns HTTP 500 Internal Server Error with generic error message
- **Postcondition**: No transaction is created

#### A5: Malformed Request
- **Trigger**: In step 2, the request body contains invalid JSON syntax
- **Steps**:
  1. System attempts to parse JSON request body
  2. JSON parsing fails due to malformed syntax
  3. System throws JSON parsing exception
  4. Global exception handler catches the exception
  5. System returns HTTP 400 Bad Request with message about JSON syntax error
- **Postcondition**: No transaction is created

#### A6: Idempotency Key Returns Cached Response
- **Trigger**: In step 4, an idempotency key is provided and a cached response exists for the same key and request hash
- **Steps**:
  1. System validates idempotency key format (must be valid UUID)
  2. System computes hash of request body
  3. System checks idempotency repository for cached response
  4. System finds cached response matching idempotency key and request hash
  5. System returns HTTP 200 with cached response (same transaction ID as original request)
- **Postcondition**: No new transaction is created; original transaction response is returned

#### A7: Idempotency Key Conflict
- **Trigger**: In step 4, an idempotency key is provided that was previously used with different request data
- **Steps**:
  1. System validates idempotency key format (must be valid UUID)
  2. System computes hash of current request body
  3. System checks idempotency repository
  4. System finds that idempotency key exists but with a different request hash
  5. System returns HTTP 409 Conflict with error message: "Idempotency key already used with different request parameters"
- **Postcondition**: No transaction is created; original transaction remains unchanged

#### A8: Invalid Idempotency Key Format
- **Trigger**: In step 4, an idempotency key is provided but it is not a valid UUID format
- **Steps**:
  1. System attempts to validate idempotency key format
  2. System determines key is not a valid UUID
  3. System returns HTTP 400 Bad Request with error message: "Invalid idempotency key format"
- **Postcondition**: No transaction is created

---

## UC-002: Retrieve All Transactions

### Use Case Information
- **Use Case ID**: UC-002
- **Use Case Name**: Retrieve All Transactions
- **Primary Actor**: External System/User
- **Secondary Actors**: None
- **Preconditions**: 
  - System is operational and database is accessible
- **Postconditions**: 
  - All transactions are retrieved and returned
  - Transaction retrieval metric is recorded

### Basic Flow
1. **Actor requests all transactions**: The actor sends a GET request to `/transactions` endpoint.

2. **System validates feature flag**: The system checks if the "get-all-transactions" feature flag is enabled.

3. **System tracks metrics**: The system records a "transactions.fetched" metric for monitoring purposes.

4. **System retrieves transactions**: The system queries the database to retrieve all stored transactions.

5. **System maps entities to domain objects**: The system converts database entities to domain objects for each retrieved transaction.

6. **System returns transaction list**: The system returns HTTP 200 with a list of all transactions, or an empty list if no transactions exist.

### Alternative Flows

#### A1: Feature Flag Disabled
- **Trigger**: In step 2, the "get-all-transactions" feature flag is disabled
- **Steps**:
  1. System checks feature flag and determines it is disabled
  2. System throws FeatureFlagDisabledException
  3. Global exception handler catches the exception
  4. System returns HTTP 403 Forbidden with error message: "Feature 'get-all-transactions' is disabled"
- **Postcondition**: No transactions are returned

#### A2: Database Error
- **Trigger**: In step 4, the database is unavailable or query fails
- **Steps**:
  1. System attempts to query database
  2. Database operation fails
  3. System throws database exception
  4. Global exception handler catches the exception
  5. System returns HTTP 500 Internal Server Error with generic error message
- **Postcondition**: No transactions are returned

#### A3: No Transactions Found
- **Trigger**: In step 4, the database contains no transactions
- **Steps**:
  1. System queries database
  2. Database returns empty result set
  3. System creates empty list
  4. System returns HTTP 200 with empty list: []
- **Postcondition**: Empty list is returned (this is considered normal operation)

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

2. **System validates request format**: The system attempts to parse the JSON request body.

3. **System validates request structure**: The system validates the request structure and required fields.

4. **System validates field types**: The system validates field data types.

5. **System returns error response**: The system returns HTTP 400 Bad Request with detailed error information about what was invalid.

### Alternative Flows

#### A1: Malformed JSON
- **Trigger**: In step 2, the request body contains invalid JSON syntax
- **Steps**:
  1. System attempts to parse JSON request body
  2. JSON parsing fails due to malformed syntax
  3. System throws JSON parsing exception
  4. Global exception handler catches the exception
  5. System returns HTTP 400 Bad Request with message about JSON syntax error
- **Postcondition**: Request is rejected

#### A2: Missing Required Fields
- **Trigger**: In step 3, the request is missing required fields
- **Steps**:
  1. System validates request structure
  2. System identifies missing required fields
  3. System throws validation exception
  4. Global exception handler catches the exception
  5. System returns HTTP 400 Bad Request with message listing missing fields
- **Postcondition**: Request is rejected

#### A3: Invalid Field Types
- **Trigger**: In step 4, the request contains fields with incorrect data types
- **Steps**:
  1. System validates field types
  2. System identifies type mismatches
  3. System throws validation exception
  4. Global exception handler catches the exception
  5. System returns HTTP 400 Bad Request with message about type errors
- **Postcondition**: Request is rejected

---

## UC-006: Cleanup Expired Idempotency Keys

### Use Case Information
- **Use Case ID**: UC-006
- **Use Case Name**: Cleanup Expired Idempotency Keys
- **Primary Actor**: System (Scheduled Task)
- **Secondary Actors**: None
- **Preconditions**: 
  - Application is running
  - Scheduling is enabled (`@EnableScheduling` in `LedgerServiceApplication`)
  - Idempotency repository is accessible
- **Postconditions**: 
  - Expired idempotency keys are deleted from storage
  - Database size is maintained within bounds
  - Cleanup operation is logged

### Basic Flow
1. **Scheduled task triggers**: The `IdempotencyCleanupScheduler` runs automatically every hour (3600000 milliseconds) via Spring's `@Scheduled` annotation.

2. **System logs cleanup start**: The system logs an informational message indicating that scheduled cleanup of expired idempotency keys has started.

3. **System calls cleanup method**: The system calls `IdempotencyRepositoryPort.deleteExpiredKeys()` to remove expired keys.

4. **Repository deletes expired keys**: The repository adapter (e.g., `DatabaseIdempotencyAdapter`) executes a database query to delete all idempotency keys where `expiresAt` is before the current time.

5. **System logs cleanup completion**: The system logs an informational message with the duration of the cleanup operation.

6. **System continues normal operation**: The cleanup operation completes without affecting normal request processing.

### Alternative Flows

#### A1: Cleanup Error
- **Trigger**: In step 3 or 4, an error occurs during cleanup (e.g., database connection issue)
- **Steps**:
  1. System attempts to delete expired keys
  2. An exception is thrown (e.g., database connection failure)
  3. System catches the exception in the scheduler
  4. System logs an error message with the exception details and duration
  5. System continues normal operation (cleanup failure doesn't affect request processing)
- **Postcondition**: Expired keys remain in storage until next cleanup attempt

#### A2: No Expired Keys
- **Trigger**: In step 4, there are no expired keys to delete
- **Steps**:
  1. System executes cleanup query
  2. Database query returns zero rows to delete
  3. System logs cleanup completion with zero deletions
  4. System continues normal operation
- **Postcondition**: No keys are deleted (normal operation when no keys are expired)

### Notes
- **Frequency**: Cleanup runs every hour by default (configurable via `@Scheduled` annotation)
- **Non-Blocking**: Cleanup runs in background and doesn't block request processing
- **Idempotent**: Multiple cleanup runs are safe (deleting already-deleted keys has no effect)
- **TTL**: Idempotency keys expire after 24 hours by default (configurable)
- **Location**: Scheduler is located in `adapters/out/scheduling/IdempotencyCleanupScheduler`
- **Port Method**: Uses `IdempotencyRepositoryPort.deleteExpiredKeys()` interface method

---

## Cross-Cutting Concerns

### Metrics Collection
All use cases automatically collect metrics through AOP aspects:
- **UC-001**: `transactions.created` counter, `idempotency.requests.total` (if idempotency key present), `idempotency.cache.hits` (if cached), `idempotency.conflicts` (if conflict detected)
- **UC-002**: `transactions.fetched` counter
- **UC-003**: `health.checks` counter (if configured)
- **UC-006**: No metrics collected (background maintenance task)
- **All**: `http.server.requests` metrics (Spring Boot default)

### Feature Flag Management
- **UC-001**: Controlled by `feature.create-transaction.enabled`
- **UC-002**: Controlled by `feature.get-all-transactions.enabled`
- **UC-003**: No feature flag (always available)
- **UC-004**: No feature flag (always available)
- **UC-005**: No feature flag (always available)
- **UC-006**: No feature flag (always runs when scheduling is enabled)

### Error Handling
All use cases follow consistent error handling patterns:
- **Validation Errors**: HTTP 400 Bad Request
- **Feature Flag Errors**: HTTP 403 Forbidden
- **Idempotency Conflicts**: HTTP 409 Conflict
- **System Errors**: HTTP 500 Internal Server Error
- **Not Found**: HTTP 404 Not Found
- **Service Unavailable**: HTTP 503 Service Unavailable

### Security Considerations
- All endpoints are currently unsecured (no authentication/authorization)
- Input validation prevents injection attacks
- Error messages don't expose sensitive system information
- Feature flags provide runtime access control
