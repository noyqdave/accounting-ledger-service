# Best Practices Guide: Use Case Specifications & BDD

## Overview
This guide captures best practices for writing use case specifications and BDD scenarios, derived from real project experience and industry standards.

---

## Use Case Specifications

### 1. Preconditions vs Runtime Validation

#### ✅ **Correct Approach**
- **Preconditions**: Only system operational requirements
  - "System is operational and database is accessible"
  - "Application server is running"
- **Runtime Validation**: Feature flags, input validation, business rules
  - Checked in basic flow steps
  - Trigger alternative flows when conditions fail

#### ❌ **Anti-Pattern**
```markdown
- **Preconditions**: 
  - Feature flag "create-transaction" is enabled  # WRONG - this is runtime validation
  - System is operational
```

#### ✅ **Correct Pattern**
```markdown
- **Preconditions**: 
  - System is operational and database is accessible  # CORRECT - system requirement
```

### 2. Clean Basic Flows

#### ✅ **Correct Approach**
- Describe only the happy path
- Linear progression without conditional logic
- No branching statements

#### ❌ **Anti-Pattern**
```markdown
2. **System validates feature flag**: The system checks if the "create-transaction" feature flag is enabled. If enabled, processing continues to step 3. If disabled, the system follows alternative flow A1.
```

#### ✅ **Correct Pattern**
```markdown
2. **System validates feature flag**: The system checks if the "create-transaction" feature flag is enabled.
```

### 3. Alternative Flow Triggers

#### ✅ **Correct Approach**
- Alternative flows contain their own trigger descriptions
- Specific step references
- Clear condition descriptions

#### ✅ **Correct Pattern**
```markdown
#### A1: Feature Flag Disabled
- **Trigger**: In step 3, the "create-transaction" feature flag is disabled
- **Steps**:
  1. System checks feature flag and determines it is disabled
  2. System throws FeatureFlagDisabledException
  3. Global exception handler catches the exception
  4. System returns HTTP 403 Forbidden with error message
```

### 4. Exception Flows vs Alternative Flows

#### ✅ **Correct Approach**
- **Alternative Flows**: Specific validation failures or business logic exceptions during execution
- **Exception Flows**: Only actual exceptions that occur during execution
- **Precondition Failures**: Not included in use case (system unavailable, etc.)

#### ❌ **Anti-Pattern**
```markdown
### Exception Flows
- **System Unavailable**: If the application server is down, the request will fail with connection error
```

#### ✅ **Correct Pattern**
```markdown
### Exception Flows
- **Database Connection Lost**: If database connection fails during transaction persistence
```

### 5. Logical Separation of Concerns

#### ✅ **Correct Structure**
- **Preconditions**: What must be true before use case starts
- **Basic Flow**: Clean, linear happy path
- **Alternative Flows**: Specific failure scenarios with clear triggers
- **Exception Flows**: Only execution exceptions

---

## BDD (Behavior-Driven Development)

### 1. Problem Space vs Solution Space

#### ✅ **Correct Approach (Problem Space)**
- Describe business intent and outcomes
- Use business language stakeholders understand
- Focus on what the business gets

#### ❌ **Anti-Pattern (Solution Space)**
```gherkin
Given the system is operational and the database is accessible
And the "create-transaction" feature flag is enabled
When I send a POST request to "/transactions" with the following data:
```json
{
  "amount": 100.00,
  "description": "Office supplies",
  "type": "EXPENSE"
}
```
Then the system should validate the request format
And the system should validate the feature flag
```

#### ✅ **Correct Pattern (Problem Space)**
```gherkin
Given I want to record a business expense
And the transaction amount is $100.00
And the transaction description is "Office supplies"
And the transaction type is "EXPENSE"
When I create the transaction
Then the transaction should be recorded in the ledger
And the transaction should show the correct amount of $100.00
```

### 2. Black Box vs White Box

#### ✅ **Correct Approach (Black Box)**
- Focus on outcomes and business value
- Describe what the system delivers
- Avoid internal implementation details

#### ❌ **Anti-Pattern (White Box)**
```gherkin
Then the system should validate the request format
And the system should track the "transactions.created" metric
And the system should create a domain object with:
- A generated UUID as identifier
- Current timestamp as creation date
And the system should persist the transaction to the database
And the system should return HTTP 200 with the created transaction object
```

#### ✅ **Correct Pattern (Black Box)**
```gherkin
Then the transaction should be recorded in the ledger
And the transaction should have a unique identifier
And the transaction should show the correct amount of $100.00
And the transaction should show the description "Office supplies"
And the transaction should be marked as an expense
And the transaction should have a timestamp showing when it was created
```

### 3. DRY (Don't Repeat Yourself)

#### ✅ **Correct Approach**
- Reference existing documentation
- Don't repeat business rules already defined
- Keep scenarios focused on behavior

#### ❌ **Anti-Pattern**
```markdown
## Business Rules
- All transactions must have a positive amount
- All transactions must have a description
- Each transaction gets a unique identifier
- Each transaction is timestamped when created
- Transactions are permanently recorded in the ledger
```

#### ✅ **Correct Pattern**
```markdown
## Notes
- These scenarios test the basic flow (happy path) of UC-001: Create Transaction
- Business rules and constraints are defined in the use case specifications
- Scenarios focus on different data variations to ensure system robustness
```

### 4. Proper Gherkin Structure

#### ✅ **Correct Pattern**
```gherkin
Feature: Create Transaction

Scenario: Successfully Create an Expense Transaction
Given I want to record a business expense
And the transaction amount is $100.00
And the transaction description is "Office supplies"
And the transaction type is "EXPENSE"
When I create the transaction
Then the transaction should be recorded in the ledger
And the transaction should have a unique identifier
And the transaction should show the correct amount of $100.00
And the transaction should show the description "Office supplies"
And the transaction should be marked as an expense
And the transaction should have a timestamp showing when it was created
```

### 5. Business Language Guidelines

#### ✅ **Use Business Terms**
- "record a business expense" (not "send POST request")
- "transaction should be recorded in the ledger" (not "persist to database")
- "unique identifier" (not "UUID")
- "timestamp showing when it was created" (not "current timestamp")

#### ✅ **Focus on Outcomes**
- What the business gets
- What the user experiences
- What value is delivered

---

## Common Anti-Patterns to Avoid

### Use Case Specifications
1. **Feature flags in preconditions** → Move to runtime validation
2. **Conditional logic in basic flows** → Keep basic flows clean
3. **System unavailable in exception flows** → This is a precondition failure
4. **Missing step references in alternative flows** → Always specify which step triggers the alternative

### BDD Scenarios
1. **Technical implementation details** → Focus on business intent
2. **JSON/HTTP details** → Use business language
3. **Internal system behavior** → Describe outcomes
4. **Repeating existing documentation** → Reference instead of repeat
5. **White box testing language** → Use black box approach

---

## Quality Checklist

### Use Case Specifications
- [ ] Preconditions contain only system operational requirements
- [ ] Basic flow is clean and linear without conditional logic
- [ ] Alternative flows have clear triggers with step references
- [ ] Exception flows contain only execution exceptions
- [ ] No redundant information with other documentation

### BDD Scenarios
- [ ] Written in problem space (business intent)
- [ ] Use black box language (outcomes, not implementation)
- [ ] Follow DRY principle (reference, don't repeat)
- [ ] Use business language stakeholders understand
- [ ] Focus on what the business gets, not how it's delivered

---

## References
- Use case specifications should reference: [Use Case Specifications](use-case-specifications.md)
- BDD scenarios should reference: [BDD Scenarios](bdd-scenarios.md)
- Architecture documentation: [Architecture Overview](ARCHITECTURE.md)
