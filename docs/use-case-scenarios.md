# Use Case Scenarios - Key System Interactions

## Scenario 1: Create a New Expense Transaction

### Overview
**Actor**: External System/User  
**Goal**: Record a new expense transaction in the ledger  
**Precondition**: System is operational and available

### Detailed Flow

![Transaction Creation Sequence](diagrams/use-case-creation.mmd)

### Success Criteria
- Transaction is created with a unique identifier
- Amount and description are preserved
- Timestamp shows when the transaction was created
- Transaction type is correctly set to expense
- User receives confirmation with created transaction details

### Error Scenarios
- **Invalid Amount**: Transaction is not created if amount is zero or negative
- **Empty Description**: Transaction is not created if description is missing or empty
- **Feature Disabled**: Transaction creation is unavailable if the feature is disabled

---

## Scenario 2: Retrieve All Transactions

### Overview
**Actor**: External System/User  
**Goal**: Get complete list of all transactions in the ledger  
**Precondition**: System is operational and available

### Detailed Flow

![Transaction Retrieval Sequence](diagrams/use-case-retrieval.mmd)

### Success Criteria
- All transactions are returned
- Response format is consistent
- User receives the complete list of transactions
- Empty list returned if no transactions exist

### Error Scenarios
- **Feature Disabled**: Transaction retrieval is unavailable if the feature is disabled
- **System Error**: Transaction retrieval fails if the system encounters an error

---

## Scenario 3: Feature Unavailable

### Overview
**Actor**: External System/User  
**Goal**: Attempt to use a disabled feature  
**Precondition**: System is operational and available

### Detailed Flow

When a feature is unavailable:
1. User attempts to use the feature
2. System checks whether the feature is currently available
3. System determines the feature is not available
4. System indicates the requested feature is unavailable
5. System returns an error message explaining the feature is disabled

### Success Criteria
- Request is properly rejected
- User receives clear error message that the feature is disabled
- No business logic execution occurs
- No transaction processing takes place

---

## Scenario 4: Invalid Transaction Data

### Overview
**Actor**: External System/User  
**Goal**: Attempt to create transaction with invalid data  
**Precondition**: System is operational and available

### Detailed Flow

![Invalid Data Sequence](diagrams/use-case-invalid-data.mmd)

### Success Criteria
- Validation errors are caught
- User receives clear error message indicating validation failure
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
- Health check request is processed
- Status indicates system is operational
- Data storage connectivity is verified
- Response includes component health details

---

## Cross-Cutting Concerns

### Metrics Collection
All business operations automatically track metrics:
- `transactions.created`: Counter for created transactions
- `transactions.fetched`: Counter for retrieved transactions
- `http.server.requests`: HTTP request metrics (Spring Boot default)

### Feature Availability Management
- Runtime configuration controls feature availability
- Features can be enabled or disabled without code changes
- Graceful degradation when features are disabled (returns error message explaining feature is unavailable)

### Error Handling
- Global exception handler for consistent error responses
- Validation errors return detailed error messages
- Feature availability errors return explanation that feature is disabled
- System errors return generic error messages
