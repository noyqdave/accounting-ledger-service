Feature: Retrieve All Transactions
  As a business user
  I want to retrieve all transactions
  So that I can view my complete transaction history

  Scenario: Retrieve All Transactions When Ledger is Empty
    Given the ledger has no transactions
    When I retrieve all transactions
    Then I should receive an empty list
    And the response status should be 200

  Scenario: Retrieve All Transactions After Creating Multiple
    Given I have created the following transactions:
      | amount | description        | type    |
      | 100.00 | Office supplies    | EXPENSE |
      | 2500.00 | Client payment   | REVENUE |
      | 99.99  | Coffee and snacks | EXPENSE |
    When I retrieve all transactions
    Then I should receive 3 transactions
    And the response should contain a transaction with amount 100.00 and description "Office supplies"
    And the response should contain a transaction with amount 2500.00 and description "Client payment"
    And the response should contain a transaction with amount 99.99 and description "Coffee and snacks"
    And each transaction should have an id
    And each transaction should have an amount
    And each transaction should have a description
    And each transaction should have a type
    And each transaction should have a date

  Scenario: Retrieve Single Transaction
    Given I have created the following transactions:
      | amount | description     | type    |
      | 100.00 | Office supplies | EXPENSE |
    When I retrieve all transactions
    Then I should receive 1 transactions
    And each transaction should have an id
    And each transaction should have an amount
    And each transaction should have a description
    And each transaction should have a type
    And each transaction should have a date

  Scenario: Retrieve Two Transactions
    Given I have created the following transactions:
      | amount | description     | type    |
      | 100.00 | Office supplies | EXPENSE |
      | 200.00 | Client payment   | REVENUE |
    When I retrieve all transactions
    Then I should receive 2 transactions
    And each transaction should have an id
    And each transaction should have an amount
    And each transaction should have a description
    And each transaction should have a type
    And each transaction should have a date

  Scenario: Retrieve Transactions Verifies All Fields
    Given I have created the following transactions:
      | amount  | description              | type    |
      | 1500.00 | Monthly subscription    | EXPENSE |
      | 50000.00 | Quarterly license renewal | EXPENSE |
      | 15000.00 | Monthly retainer        | REVENUE |
    When I retrieve all transactions
    Then I should receive 3 transactions
    And all transactions should have valid data
    And the response status should be 200

  @ff_disabled
  Scenario: Retrieve All Transactions When Feature is Disabled
    Given the ledger has no transactions
    When I retrieve all transactions
    Then the response status should be 403
    And I should receive an error message that the feature is disabled
