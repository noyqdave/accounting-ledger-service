# BDD Acceptance Test Scenarios - Create Transaction

## Overview
These BDD scenarios are derived from UC-001: Create Transaction use case, focusing on the basic flow (happy path) and key alternative flows.

## Feature: Create Transaction

### Scenario Outline: Create Transaction with Various Data Types
**Given** I want to record a <transaction_category>  
**And** the transaction amount is <amount>  
**And** the transaction description is "<description>"  
**And** the transaction type is "<type>"  
**When** I create the transaction  
**Then** the transaction should be recorded in the ledger  
**And** the transaction should have a unique identifier  
**And** the transaction should show the correct amount of <amount>  
**And** the transaction should show the description "<description>"  
**And** the transaction should be marked as <type_lowercase>  
**And** the transaction should have a timestamp showing when it was created

#### Examples:
| transaction_category | amount | description | type | type_lowercase |
|---------------------|--------|-------------|------|----------------|
| business expense | $100.00 | Office supplies | EXPENSE | an expense |
| business revenue | $2,500.00 | Client payment for project work | REVENUE | revenue |
| small business expense | $99.99 | Coffee and snacks | EXPENSE | an expense |
| recurring business expense | $1,500.00 | Monthly subscription for cloud hosting services including compute, storage, and networking resources | EXPENSE | an expense |
| major business expense | $50,000.00 | Quarterly software license renewal | EXPENSE | an expense |
| business revenue | $15,000.00 | Monthly retainer payment | REVENUE | revenue |

---

## Individual Scenarios (Alternative Format)

### Scenario 1: Successfully Create an Expense Transaction
**Given** I want to record a business expense  
**And** the transaction amount is $100.00  
**And** the transaction description is "Office supplies"  
**And** the transaction type is "EXPENSE"  
**When** I create the transaction  
**Then** the transaction should be recorded in the ledger  
**And** the transaction should have a unique identifier  
**And** the transaction should show the correct amount of $100.00  
**And** the transaction should show the description "Office supplies"  
**And** the transaction should be marked as an expense  
**And** the transaction should have a timestamp showing when it was created

### Scenario 2: Successfully Create a Revenue Transaction
**Given** I want to record business revenue  
**And** the transaction amount is $2,500.00  
**And** the transaction description is "Client payment for project work"  
**And** the transaction type is "REVENUE"  
**When** I create the transaction  
**Then** the transaction should be recorded in the ledger  
**And** the transaction should have a unique identifier  
**And** the transaction should show the correct amount of $2,500.00  
**And** the transaction should show the description "Client payment for project work"  
**And** the transaction should be marked as revenue  
**And** the transaction should have a timestamp showing when it was created

### Scenario 3: Create Transaction with Decimal Amount
**Given** I want to record a small business expense  
**And** the transaction amount is $99.99  
**And** the transaction description is "Coffee and snacks"  
**And** the transaction type is "EXPENSE"  
**When** I create the transaction  
**Then** the transaction should be recorded in the ledger  
**And** the transaction should have a unique identifier  
**And** the transaction should show the correct amount of $99.99  
**And** the transaction should show the description "Coffee and snacks"  
**And** the transaction should be marked as an expense  
**And** the transaction should have a timestamp showing when it was created

### Scenario 4: Create Transaction with Detailed Description
**Given** I want to record a recurring business expense  
**And** the transaction amount is $1,500.00  
**And** the transaction description is "Monthly subscription for cloud hosting services including compute, storage, and networking resources"  
**And** the transaction type is "EXPENSE"  
**When** I create the transaction  
**Then** the transaction should be recorded in the ledger  
**And** the transaction should have a unique identifier  
**And** the transaction should show the correct amount of $1,500.00  
**And** the transaction should show the full description "Monthly subscription for cloud hosting services including compute, storage, and networking resources"  
**And** the transaction should be marked as an expense  
**And** the transaction should have a timestamp showing when it was created

### Scenario 5: Create Transaction with Large Amount
**Given** I want to record a major business expense  
**And** the transaction amount is $50,000.00  
**And** the transaction description is "Quarterly software license renewal"  
**And** the transaction type is "EXPENSE"  
**When** I create the transaction  
**Then** the transaction should be recorded in the ledger  
**And** the transaction should have a unique identifier  
**And** the transaction should show the correct amount of $50,000.00  
**And** the transaction should show the description "Quarterly software license renewal"  
**And** the transaction should be marked as an expense  
**And** the transaction should have a timestamp showing when it was created

## Scenario Outline Benefits

### Advantages of Scenario Outline Format:
- **DRY Principle**: Single scenario template with multiple data examples
- **Easy Maintenance**: Add new test cases by adding rows to the examples table
- **Clear Coverage**: Easy to see all test variations at a glance
- **Parameterized Testing**: BDD tools can execute each row as a separate test
- **Business Focus**: Examples table shows business context for each test case

### Test Coverage:
- **Transaction Types**: Both EXPENSE and REVENUE (only two types supported by the system)
- **Amount Ranges**: Small ($99.99) to Large ($50,000.00)
- **Description Lengths**: Short to detailed descriptions
- **Business Context**: Different types of business transactions (expenses and revenue)

### Mapping to Use Case:
- **Scenario Outline**: Tests the complete basic flow (steps 1-8) of UC-001
- **Each Example**: Validates the same business process with different data
- **Outcomes**: All examples should result in successful transaction creation

## Notes
- These scenarios test the basic flow (happy path) of UC-001: Create Transaction
- Business rules and constraints are defined in the use case specifications
- Scenario outline format provides comprehensive coverage with minimal duplication
- Each example row represents a complete test case that can be executed independently
