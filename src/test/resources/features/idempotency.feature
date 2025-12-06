Feature: Transaction Idempotency
  As a client application
  I want to safely retry transaction creation requests
  So that network errors don't result in duplicate transactions

  Scenario Outline: Successfully Create Transaction with Idempotency Key
    Given I want to record a transaction
    And the transaction amount is <amount>
    And the transaction description is "<description>"
    And the transaction type is "<type>"
    And I provide an idempotency key "<key>"
    When I create the transaction
    Then the transaction should be recorded in the ledger
    And the transaction should have a unique identifier
    And the transaction should show the correct amount of <amount>
    And the transaction should show the description "<description>"
    And the transaction should be marked as <type>
    And the transaction should have a timestamp showing when it was created

    Examples:
      | amount | description | type | key | # Test Intent |
      | 100.00 | Office supplies | EXPENSE | 550e8400-e29b-41d4-a716-446655440000 | # Test basic expense with idempotency |
      | 2500.00 | Client payment | REVENUE | 660e8400-e29b-41d4-a716-446655440001 | # Test revenue with idempotency |
      | 99.99 | Coffee and snacks | EXPENSE | 770e8400-e29b-41d4-a716-446655440002 | # Test decimal amounts with idempotency |

  Scenario: Retry Request with Same Idempotency Key Returns Same Response
    Given I want to record a transaction
    And the transaction amount is 100.00
    And the transaction description is "Office supplies"
    And the transaction type is "EXPENSE"
    And I provide an idempotency key "880e8400-e29b-41d4-a716-446655440003"
    When I create the transaction
    Then the transaction should be recorded in the ledger
    And the transaction should have a unique identifier
    And I store the transaction identifier
    When I create the same transaction again with the same idempotency key "880e8400-e29b-41d4-a716-446655440003"
    Then the response should contain the same transaction identifier as stored
    And no duplicate transaction should be created in the ledger

  Scenario: Retry Request with Different Amount Returns Conflict Error
    Given I want to record a transaction
    And the transaction amount is 100.00
    And the transaction description is "Office supplies"
    And the transaction type is "EXPENSE"
    And I provide an idempotency key "aa0e8400-e29b-41d4-a716-446655440005"
    When I create the transaction
    Then the transaction should be recorded in the ledger
    When I try to create a transaction with amount 200.00
    And the transaction description is "Office supplies"
    And the transaction type is "EXPENSE"
    And I provide the same idempotency key "aa0e8400-e29b-41d4-a716-446655440005"
    When I send the transaction request with the same idempotency key
    Then the request should be rejected with a conflict error
    And I should receive an error message about the idempotency key conflict

  Scenario: Retry Request with Different Description Returns Conflict Error
    Given I want to record a transaction
    And the transaction amount is 100.00
    And the transaction description is "Office supplies"
    And the transaction type is "EXPENSE"
    And I provide an idempotency key "bb0e8400-e29b-41d4-a716-446655440006"
    When I create the transaction
    Then the transaction should be recorded in the ledger
    When I try to create a transaction with amount 100.00
    And the transaction description is "Different supplies"
    And the transaction type is "EXPENSE"
    And I provide the same idempotency key "bb0e8400-e29b-41d4-a716-446655440006"
    When I send the transaction request with the same idempotency key
    Then the request should be rejected with a conflict error
    And I should receive an error message about the idempotency key conflict

  Scenario: Retry Request with Different Type Returns Conflict Error
    Given I want to record a transaction
    And the transaction amount is 100.00
    And the transaction description is "Office supplies"
    And the transaction type is "EXPENSE"
    And I provide an idempotency key "cc0e8400-e29b-41d4-a716-446655440007"
    When I create the transaction
    Then the transaction should be recorded in the ledger
    When I try to create a transaction with amount 100.00
    And the transaction description is "Office supplies"
    And the transaction type is "REVENUE"
    And I provide the same idempotency key "cc0e8400-e29b-41d4-a716-446655440007"
    When I send the transaction request with the same idempotency key
    Then the request should be rejected with a conflict error
    And I should receive an error message about the idempotency key conflict

  Scenario: Different Idempotency Keys Create Independent Transactions
    Given I want to record a transaction
    And the transaction amount is 100.00
    And the transaction description is "Office supplies"
    And the transaction type is "EXPENSE"
    And I provide an idempotency key "dd0e8400-e29b-41d4-a716-446655440008"
    When I create the transaction
    Then the transaction should be recorded in the ledger
    And the transaction should have a unique identifier
    And I store the transaction identifier
    When I create a transaction with the same amount 100.00
    And the transaction description is "Office supplies"
    And the transaction type is "EXPENSE"
    And I provide a different idempotency key "ee0e8400-e29b-41d4-a716-446655440009"
    When I create the transaction with the updated idempotency key
    Then a new transaction should be created in the ledger
    And the new transaction should have a different identifier than the stored one

  Scenario: Request Without Idempotency Key Works Normally
    Given I want to record a transaction
    And the transaction amount is 100.00
    And the transaction description is "Office supplies"
    And the transaction type is "EXPENSE"
    And I do not provide an idempotency key
    When I create the transaction
    Then the transaction should be recorded in the ledger
    And the transaction should have a unique identifier
    And I store the transaction identifier
    When I create the same transaction again without an idempotency key
    Then a new transaction should be created in the ledger
    And the new transaction should have a different identifier than the stored one
