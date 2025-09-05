Feature: Create Transaction with Feature Disabled
  As a business user
  I want to understand what happens when features are disabled
  So that I can handle system maintenance scenarios

  Scenario: Create Transaction When Feature is Disabled
    Given I want to record a transaction with feature disabled
    And the transaction amount is 100.00 with feature disabled
    And the transaction description is "Test transaction" with feature disabled
    And the transaction type is "EXPENSE" with feature disabled
    And the create transaction feature is disabled
    When I create the transaction with feature disabled
    Then the transaction should not be created with feature disabled
    And I should receive an error message that the feature is disabled
