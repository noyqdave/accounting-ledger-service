# Use Case Scenarios - Key System Interactions

## Scenario 1: Create a New Expense Transaction

### Overview
**Actor**: External System/User  
**Goal**: Record a new expense transaction in the ledger  
**Precondition**: Feature flag "create-transaction" is enabled

### Detailed Flow

![Transaction Creation Sequence](diagrams/use-case-creation.mmd)

### Success Criteria
- Transaction is created with valid UUID
- Amount and description are preserved
- Timestamp is set to current time
- Transaction type is correctly set to EXPENSE
- HTTP 200 response with created transaction

### Error Scenarios
- **Invalid Amount**: HTTP 400 if amount ≤ 0
- **Empty Description**: HTTP 400 if description is null/empty
- **Feature Disabled**: HTTP 403 if feature flag is disabled

---

## Scenario 2: Retrieve All Transactions

### Overview
**Actor**: External System/User  
**Goal**: Get complete list of all transactions in the ledger  
**Precondition**: Feature flag "get-all-transactions" is enabled

### Detailed Flow

![Transaction Retrieval Sequence](diagrams/use-case-retrieval.mmd)

### Success Criteria
- All transactions are returned
- Response format is consistent
- HTTP 200 status code
- Empty list returned if no transactions exist

### Error Scenarios
- **Feature Disabled**: HTTP 403 if feature flag is disabled
- **Database Error**: HTTP 500 if database is unavailable

---

## Scenario 3: Feature Flag Disabled

### Overview
**Actor**: External System/User  
**Goal**: Attempt to use a disabled feature  
**Precondition**: Feature flag is disabled in configuration

### Detailed Flow

When a feature flag is disabled:
1. HTTP request arrives at FeatureFlagFilter
2. Filter checks feature flag via FeatureFlagService
3. FeatureFlagService reads from application.yml configuration
4. If disabled, FeatureFlagFilter returns HTTP 403 Forbidden with JSON error
5. Request never reaches the controller

### Success Criteria
- Request is properly rejected at filter level
- HTTP 403 Forbidden status
- Clear error message: `{"error": "Feature is disabled"}`
- No business logic execution
- No controller method invocation

---

## Scenario 4: Invalid Transaction Data

### Overview
**Actor**: External System/User  
**Goal**: Attempt to create transaction with invalid data  
**Precondition**: Feature flag is enabled

### Detailed Flow

![Invalid Data Sequence](diagrams/use-case-invalid-data.mmd)

### Success Criteria
- Validation errors are caught
- HTTP 400 Bad Request status
- Clear error message indicating validation failure
- No transaction is created

---

## Scenario 5: System Health Check

### Overview
**Actor**: Monitoring System  
**Goal**: Verify system health and availability  
**Precondition**: Application is running

### Detailed Flow

![Health Check Sequence](diagrams/use-case-health-check.mmd)

### Success Criteria
- Health endpoint responds with HTTP 200
- Status indicates "UP"
- Database connectivity is verified
- Response includes component health details

---

## Cross-Cutting Concerns

### Metrics Collection
All business operations automatically track metrics:
- `transactions.created`: Counter for created transactions
- `transactions.fetched`: Counter for retrieved transactions
- `http.server.requests`: HTTP request metrics (Spring Boot default)

### Feature Flag Management
- Runtime configuration via `application.yml`
- Filter-based enforcement via `FeatureFlagFilter` at HTTP layer
- Endpoint-to-feature-flag mappings configurable in `application.yml`
- Graceful degradation when features are disabled (returns 403 with JSON error)

### Error Handling
- Global exception handler for consistent error responses
- Validation errors return HTTP 400
- Feature flag errors return HTTP 403
- System errors return HTTP 500
