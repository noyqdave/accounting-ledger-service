# BDD Acceptance Test Scenarios - Create Transaction

## Overview
These BDD scenarios are derived from UC-001: Create Transaction use case, focusing on the basic flow (happy path) and key alternative flows.

## Feature: Create Transaction

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

## Business Context

### Transaction Types
- **EXPENSE**: Money going out of the business (purchases, bills, operational costs)
- **REVENUE**: Money coming into the business (sales, payments, income)

### Amount Ranges
- **Small amounts**: $0.01 to $99.99 (daily expenses, small purchases)
- **Medium amounts**: $100.00 to $2,500.00 (regular business expenses, monthly costs)
- **Large amounts**: $2,500.00+ (major purchases, quarterly/annual expenses)

### Description Examples
- **Short**: "Coffee", "Lunch", "Taxi"
- **Medium**: "Office supplies", "Client meeting", "Software license"
- **Detailed**: "Monthly subscription for cloud hosting services including compute, storage, and networking resources"

## Business Rules
- All transactions must have a positive amount
- All transactions must have a description
- Each transaction gets a unique identifier
- Each transaction is timestamped when created
- Transactions are permanently recorded in the ledger
- The system should handle various amount formats and description lengths
