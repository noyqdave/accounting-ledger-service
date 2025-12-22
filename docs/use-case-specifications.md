# Use Case Specifications

## Overview

This document describes the system's behavior from a business perspective, focusing on what the system does rather than how it is implemented. These specifications are written in business language and describe interactions between users and the system without technical implementation details.

**Note on Project Context**: In this project, use case specifications were reverse engineered from existing code and observed behavior after implementation. They normalize terminology, document flows consistently, and capture exception handling patterns.

> **Visual Representation**: An activity diagram showing the basic flow and all alternative flows for UC-001 (Create Transaction) is available at [use-case-creation-activity.mmd](../diagrams/use-case-creation-activity.mmd). This diagram visualizes the decision points and flow paths described in the use case specifications below.

---

## UC-001: Create Transaction

### Use Case Information
- **Use Case ID**: UC-001
- **Use Case Name**: Create Transaction
- **Primary Actor**: External System/User
- **Secondary Actors**: None
- **Preconditions**: 
  - System is operational and available
- **Postconditions**: 
  - New transaction is recorded in the system
  - Transaction is assigned a unique identifier
  - Transaction creation is logged for monitoring

### Basic Flow
1. **User initiates transaction creation**: The user submits a request to create a new transaction, providing the transaction amount, description, and type (expense or revenue).

2. **System validates request format**: The system verifies that the request is properly formatted and can be understood.

3. **System validates feature availability**: The system checks whether transaction creation is currently available.

4. **System records transaction creation activity**: The system logs that a transaction creation attempt occurred for monitoring purposes.

5. **System validates transaction data**: The system verifies that the provided transaction data meets business rules:
   - Amount must be greater than zero
   - Description must be provided and not empty
   - Transaction type must be either expense or revenue

6. **System creates transaction record**: The system creates a new transaction record with:
   - A unique identifier assigned by the system
   - Current date and time as the creation timestamp
   - The validated amount, description, and type provided by the user

7. **System saves transaction**: The system stores the transaction record so it can be retrieved later.

8. **System confirms successful creation**: The system returns a confirmation to the user with the created transaction details including the assigned identifier and timestamp.

### Alternative Flows

#### A1: Feature Unavailable
- **Trigger**: In step 3, transaction creation is currently unavailable
- **Steps**:
  1. System determines that transaction creation is not available
  2. System indicates that the requested feature is unavailable
  3. System returns an error message explaining the feature is disabled
- **Postcondition**: No transaction is created

#### A2: Invalid Amount
- **Trigger**: In step 6, the amount is zero or negative
- **Steps**:
  1. System validates amount and determines it is invalid
  2. System indicates the amount must be positive
  3. System returns an error message explaining the validation failure
- **Postcondition**: No transaction is created

#### A3: Missing Description
- **Trigger**: In step 6, the description is missing or empty
- **Steps**:
  1. System validates description and determines it is invalid
  2. System indicates that a description is required
  3. System returns an error message explaining the validation failure
- **Postcondition**: No transaction is created

#### A4: System Error
- **Trigger**: In step 7, the system encounters an error while saving
- **Steps**:
  1. System attempts to save transaction
  2. System operation fails
  3. System indicates an internal error occurred
  4. System returns a generic error message
- **Postcondition**: No transaction is created

#### A5: Idempotency Key Provided
- **Trigger**: In step 1, the user includes an idempotency key with the request
- **Steps**:
  1. System validates that the idempotency key format is acceptable
  2. System determines the validation result and follows the appropriate alternative flow
- **Postcondition**: Depends on which alternative flow is followed

#### A5.1: Invalid Idempotency Key Format
- **Trigger**: In alternative flow A5, step 1, the idempotency key format is invalid
- **Steps**:
  1. System determines the key format is invalid
  2. System indicates the key format is unacceptable
  3. System returns an error message explaining the format requirement
- **Postcondition**: No transaction is created

#### A5.2: Valid Idempotency Key Format
- **Trigger**: In alternative flow A5, step 1, the idempotency key format is valid
- **Steps**:
  1. System verifies that the request content matches any previous request with the same key
  2. System checks for previous responses associated with this key
  3. System determines the result and follows the appropriate alternative flow
- **Postcondition**: Depends on which alternative flow is followed

#### A5.2.1: Previous Response Exists and Valid
- **Trigger**: In alternative flow A5.2, step 3, a previous successful response exists for the same key and request and is still valid
- **Steps**:
  1. System finds a previous successful response for this exact request
  2. System verifies the previous response is still valid (not expired)
  3. System returns the previous response with the same transaction details
- **Postcondition**: No new transaction is created; user receives the same response as the original request

#### A5.2.2: Idempotency Key Conflict
- **Trigger**: In alternative flow A5.2, step 3, the key was previously used with different request content
- **Steps**:
  1. System finds that the key was used before but with different request content
  2. System indicates a conflict has occurred
  3. System returns an error message explaining that the key was already used with different parameters
- **Postcondition**: No transaction is created; original transaction remains unchanged

#### A5.2.3: Previous Response Expired
- **Trigger**: In alternative flow A5.2, step 3, a previous response exists but has expired
- **Steps**:
  1. System finds a previous response but determines it has expired
  2. System removes the expired record
  3. System continues with basic flow from step 2, and after successful creation (step 8), stores the response associated with the key for a limited time
- **Postcondition**: Expired record is removed; new transaction is created with the same idempotency key

#### A5.2.4: New Idempotency Key
- **Trigger**: In alternative flow A5.2, step 3, the key is new (no previous response exists)
- **Steps**:
  1. System determines the key has not been used before
  2. System continues with basic flow from step 2, and after successful creation (step 8), stores the response associated with the key for a limited time
- **Postcondition**: Transaction is created and response is cached for future identical requests

#### A6: Malformed Request
- **Trigger**: In step 2, the request cannot be understood
- **Steps**:
  1. System attempts to understand the request
  2. System determines the request format is invalid
  3. System indicates the request format is incorrect
  4. System returns an error message explaining the format issue
- **Postcondition**: No transaction is created


---

## UC-002: Retrieve All Transactions

### Use Case Information
- **Use Case ID**: UC-002
- **Use Case Name**: Retrieve All Transactions
- **Primary Actor**: External System/User
- **Secondary Actors**: None
- **Preconditions**: 
  - System is operational and available
- **Postconditions**: 
  - All transactions are retrieved and presented to the user
  - Transaction retrieval activity is logged for monitoring

### Basic Flow
1. **User requests all transactions**: The user submits a request to retrieve all transaction records.

2. **System validates feature availability**: The system checks whether transaction retrieval is currently available.

3. **System records retrieval activity**: The system logs that a transaction retrieval attempt occurred for monitoring purposes.

4. **System retrieves transactions**: The system gathers all stored transaction records.

5. **System prepares transaction list**: The system organizes the retrieved transactions into a list format.

6. **System returns transaction list**: The system returns the complete list of transactions to the user, or an empty list if no transactions exist.

### Alternative Flows

#### A1: Feature Unavailable
- **Trigger**: In step 2, transaction retrieval is currently unavailable
- **Steps**:
  1. System determines that transaction retrieval is not available
  2. System indicates that the requested feature is unavailable
  3. System returns an error message explaining the feature is disabled
- **Postcondition**: No transactions are returned

#### A2: System Error
- **Trigger**: In step 4, the system encounters an error while retrieving transactions
- **Steps**:
  1. System attempts to retrieve transactions
  2. System operation fails
  3. System indicates an internal error occurred
  4. System returns a generic error message
- **Postcondition**: No transactions are returned

#### A3: No Transactions Found
- **Trigger**: In step 4, the system contains no transaction records
- **Steps**:
  1. System attempts to retrieve transactions
  2. System finds no transaction records exist
  3. System creates an empty list
  4. System returns the empty list
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
  - Health check functionality is enabled
- **Postconditions**: 
  - System health status is determined and reported
  - Health check activity is recorded (if configured)

### Basic Flow
1. **Monitoring system requests health status**: The monitoring system submits a request to check the system's health status.

2. **System performs health checks**: The system executes various health verification checks:
   - Application status verification
   - Data storage connectivity test
   - Resource availability checks (if configured)

3. **System determines overall health**: The system combines results from all health checks to determine the overall system status.

4. **System returns health status**: The system returns the health status information including:
   - Overall status (operational or unavailable)
   - Individual component statuses
   - Additional health details (if configured)

### Alternative Flows

#### A1: Data Storage Unavailable
- **Trigger**: Data storage connection fails
- **Steps**:
  1. System attempts data storage connectivity test
  2. Data storage connection fails
  3. System marks data storage component as unavailable
  4. System determines overall status as unavailable
  5. System returns status indicating system is unavailable with component details
- **Postcondition**: System is marked as unhealthy

#### A2: Application Issues
- **Trigger**: Application has internal issues
- **Steps**:
  1. System performs application health checks
  2. Application health check fails
  3. System marks application as unavailable
  4. System determines overall status as unavailable
  5. System returns status indicating system is unavailable with component details
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
  - API documentation functionality is enabled
- **Postconditions**: 
  - API documentation is displayed
  - User can interact with API endpoints

### Basic Flow
1. **User accesses API documentation**: The user navigates to the API documentation interface.

2. **System serves documentation**: The system presents the API documentation interface with complete API information.

3. **User views API endpoints**: The user can view all available endpoints, request/response formats, and example data.

4. **User can test endpoints**: The user can interact with the API directly through the documentation interface.

### Alternative Flows

#### A1: Documentation Unavailable
- **Trigger**: API documentation functionality is disabled
- **Steps**:
  1. User attempts to access documentation
  2. System indicates documentation is not available
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
1. **User sends invalid request**: The user submits a request with invalid data (improperly formatted, missing required information, etc.).

2. **System validates request format**: The system attempts to understand the request format.

3. **System validates request structure**: The system validates the request structure and required information.

4. **System validates data types**: The system validates that data types are correct.

5. **System returns error response**: The system returns an error message with detailed information about what was invalid.

### Alternative Flows

#### A1: Malformed Request Format
- **Trigger**: In step 2, the request format cannot be understood
- **Steps**:
  1. System attempts to understand request format
  2. System determines the format is invalid
  3. System indicates the format is incorrect
  4. System returns an error message explaining the format issue
- **Postcondition**: Request is rejected

#### A2: Missing Required Information
- **Trigger**: In step 3, the request is missing required information
- **Steps**:
  1. System validates request structure
  2. System identifies missing required information
  3. System indicates what information is missing
  4. System returns an error message listing missing information
- **Postcondition**: Request is rejected

#### A3: Invalid Data Types
- **Trigger**: In step 4, the request contains data with incorrect types
- **Steps**:
  1. System validates data types
  2. System identifies type mismatches
  3. System indicates what types are incorrect
  4. System returns an error message explaining the type errors
- **Postcondition**: Request is rejected

---

## UC-006: Maintain Idempotency Records

### Use Case Information
- **Use Case ID**: UC-006
- **Use Case Name**: Maintain Idempotency Records
- **Primary Actor**: System (Automatic Process)
- **Secondary Actors**: None
- **Preconditions**: 
  - Application is running
  - Automatic maintenance is enabled
  - Idempotency record storage is accessible
- **Postconditions**: 
  - Expired idempotency records are removed from storage
  - Storage size is maintained within acceptable bounds
  - Maintenance operation is logged

### Basic Flow
1. **Automatic maintenance triggers**: The system automatically initiates a maintenance process at regular intervals.

2. **System logs maintenance start**: The system records that maintenance of idempotency records has started.

3. **System identifies expired records**: The system identifies idempotency records that are no longer needed (have expired).

4. **System removes expired records**: The system removes all expired idempotency records from storage.

5. **System logs maintenance completion**: The system records that maintenance has completed, including how long it took.

6. **System continues normal operation**: The maintenance operation completes without affecting normal request processing.

### Alternative Flows

#### A1: Maintenance Error
- **Trigger**: In step 3 or 4, an error occurs during maintenance (e.g., storage connection issue)
- **Steps**:
  1. System attempts to remove expired records
  2. An error occurs during the operation
  3. System catches and logs the error with details
  4. System continues normal operation (maintenance failure doesn't affect request processing)
- **Postcondition**: Expired records remain in storage until next maintenance attempt

#### A2: No Expired Records
- **Trigger**: In step 4, there are no expired records to remove
- **Steps**:
  1. System executes maintenance process
  2. System finds no expired records exist
  3. System logs maintenance completion with zero records removed
  4. System continues normal operation
- **Postcondition**: No records are removed (normal operation when no records are expired)

### Notes
- **Frequency**: Maintenance runs automatically at regular intervals
- **Non-Blocking**: Maintenance runs in background and doesn't interfere with request processing
- **Idempotent**: Multiple maintenance runs are safe (removing already-removed records has no effect)
- **Retention Period**: Idempotency records are kept for a limited time (typically aligned with client retry windows)

---

## Cross-Cutting Concerns

### Activity Monitoring
All use cases automatically record activity for monitoring purposes:
- **UC-001**: Transaction creation activity is recorded
- **UC-002**: Transaction retrieval activity is recorded
- **UC-003**: Health check activity is recorded (if configured)
- **UC-006**: Maintenance activity is recorded
- **All**: General request activity is recorded

### Feature Availability Management
- **UC-001**: Controlled by transaction creation availability setting
- **UC-002**: Controlled by transaction retrieval availability setting
- **UC-003**: Always available (no availability setting)
- **UC-004**: Always available (no availability setting)
- **UC-005**: Always available (no availability setting)
- **UC-006**: Always runs when automatic maintenance is enabled

### Error Handling
All use cases follow consistent error handling patterns:
- **Validation Errors**: User input validation failures with detailed error messages
- **Feature Unavailable Errors**: Feature availability errors with explanation
- **Idempotency Conflicts**: Conflict errors when idempotency key is reused incorrectly
- **System Errors**: Internal system errors with generic error messages
- **Not Found Errors**: Resource not found errors
- **Service Unavailable Errors**: System unavailable errors

### Security Considerations
- All endpoints are currently accessible without authentication/authorization
- Input validation prevents malicious input
- Error messages don't expose sensitive system information
- Feature availability settings provide runtime access control

### Business Rules
- **Transaction Amount**: Must be greater than zero
- **Transaction Description**: Must be provided and not empty
- **Transaction Type**: Must be either expense or revenue
- **Idempotency Key Format**: Must be a universally unique identifier (UUID) in standard format (e.g., "550e8400-e29b-41d4-a716-446655440000")
- **Idempotency Key Validity**: Idempotency keys and their cached responses remain valid for 24 hours from creation
- **Response Caching**: Only successful transaction creations (status indicating success) are cached for idempotency; error responses are not cached, allowing retries


