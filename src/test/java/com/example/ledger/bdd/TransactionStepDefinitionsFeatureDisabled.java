package com.example.ledger.bdd;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@TestPropertySource(properties = {
    "feature.create-transaction.enabled=false",
    "feature.get-all-transactions.enabled=true"
})
public class TransactionStepDefinitionsFeatureDisabled {

    @LocalServerPort
    private int port;

    private Response response;
    private Map<String, Object> transactionData = new HashMap<>();
    private String baseUrl;

    @Given("I want to record a transaction with feature disabled")
    public void i_want_to_record_a_transaction_with_feature_disabled() {
        baseUrl = "http://localhost:" + port;
        RestAssured.baseURI = baseUrl;
    }

    @Given("the transaction amount is {double} with feature disabled")
    public void the_transaction_amount_is_with_feature_disabled(Double amount) {
        transactionData.put("amount", amount);
    }

    @Given("the transaction description is {string} with feature disabled")
    public void the_transaction_description_is_with_feature_disabled(String description) {
        if (!"null".equals(description)) {
            transactionData.put("description", description);
        }
    }

    @Given("the transaction type is {string} with feature disabled")
    public void the_transaction_type_is_with_feature_disabled(String type) {
        transactionData.put("type", type);
    }

    @Given("the create transaction feature is disabled")
    public void the_create_transaction_feature_is_disabled() {
        // This scenario will be run with feature flags disabled
    }

    @When("I create the transaction with feature disabled")
    public void i_create_the_transaction_with_feature_disabled() {
        response = given()
                .contentType(ContentType.JSON)
                .body(transactionData)
                .when()
                .post("/transactions");
    }

    @Then("the transaction should not be created with feature disabled")
    public void the_transaction_should_not_be_created_with_feature_disabled() {
        response.then()
                .statusCode(403);
    }

    @Given("I should receive an error message that the feature is disabled")
    public void i_should_receive_an_error_message_that_the_feature_is_disabled() {
        response.then()
                .body("error", equalTo("Feature is disabled"));
    }
}
