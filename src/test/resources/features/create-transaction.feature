Feature: Create Transaction
  As a business user
  I want to create financial transactions
  So that I can track my business expenses and revenue

  Scenario Outline: Successfully Create Transaction
    Given I want to record a transaction
    And the transaction amount is <amount>
    And the transaction description is "<description>"
    And the transaction type is "<type>"
    When I create the transaction
    Then the transaction should be recorded in the ledger
    And the transaction should have a unique identifier
    And the transaction should show the correct amount of <amount>
    And the transaction should show the description "<description>"
    And the transaction should be marked as <type>
    And the transaction should have a timestamp showing when it was created

    Examples:
      | amount | description | type | # Test Intent |
      | 100.00 | Office supplies | EXPENSE | # Test typical small business expense |
      | 2500.00 | Client payment for project work | REVENUE | # Test typical project revenue |
      | 99.99 | Coffee and snacks | EXPENSE | # Test decimal precision and small amounts |
      | 1500.00 | Monthly subscription for cloud hosting services | EXPENSE | # Test long description handling |
      | 50000.00 | Quarterly software license renewal | EXPENSE | # Test large business expense |
      | 15000.00 | Monthly retainer payment | REVENUE | # Test recurring revenue stream |

  Scenario: Create Transaction with Zero Amount
    Given I want to record a transaction
    And the transaction amount is 0.00
    And the transaction description is "Invalid transaction"
    And the transaction type is "EXPENSE"
    When I create the transaction
    Then the transaction should not be created
    And I should receive an error message about the amount being invalid

  Scenario: Create Transaction with Negative Amount
    Given I want to record a transaction
    And the transaction amount is -50.00
    And the transaction description is "Invalid transaction"
    And the transaction type is "EXPENSE"
    When I create the transaction
    Then the transaction should not be created
    And I should receive an error message about the amount being invalid

  Scenario: Create Transaction with Empty Description
    Given I want to record a transaction
    And the transaction amount is 100.00
    And the transaction description is ""
    And the transaction type is "EXPENSE"
    When I create the transaction
    Then the transaction should not be created
    And I should receive an error message about the description being required

  Scenario: Create Transaction with Null Description
    Given I want to record a transaction
    And the transaction amount is 100.00
    And the transaction description is null
    And the transaction type is "EXPENSE"
    When I create the transaction
    Then the transaction should not be created
    And I should receive an error message about the description being required

  Scenario: Create Transaction with Invalid Type
    Given I want to record a transaction
    And the transaction amount is 100.00
    And the transaction description is "Test transaction"
    And the transaction type is "INVALID"
    When I create the transaction
    Then the transaction should not be created
    And I should receive an error message about the transaction type being invalid
