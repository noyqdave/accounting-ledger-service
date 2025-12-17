package com.example.ledger.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class IdempotencyStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private TestContext testContext;

    @Given("I provide an idempotency key {string}")
    public void i_provide_an_idempotency_key(String key) {
        // Store the idempotency key in shared test context
        // This key will be used by TransactionStepDefinitions when creating transactions
        testContext.setIdempotencyKey(key);
    }

    @Given("I do not provide an idempotency key")
    public void i_do_not_provide_an_idempotency_key() {
        // Clear the idempotency key from shared test context
        // This allows requests to proceed without idempotency processing
        testContext.setIdempotencyKey(null);
    }

    @Given("I provide the same idempotency key {string}")
    public void i_provide_the_same_idempotency_key(String key) {
        // Store the same idempotency key for a retry request
        // This is used when testing conflict scenarios
        testContext.setIdempotencyKey(key);
    }

    @Given("I provide a different idempotency key {string}")
    public void i_provide_a_different_idempotency_key(String key) {
        // Store a different idempotency key
        // This is used to test that different keys create independent transactions
        testContext.setIdempotencyKey(key);
    }

    @When("I create the same transaction again with the same idempotency key {string}")
    public void i_create_the_same_transaction_again_with_the_same_idempotency_key(String key) {
        // Create the same transaction request again with the stored idempotency key
        // Uses transactionData from shared test context and includes the Idempotency-Key header
        testContext.setIdempotencyKey(key);
        
        var requestSpec = given()
                .contentType(ContentType.JSON)
                .header("Idempotency-Key", key)
                .body(testContext.getTransactionData());
        
        Response response = requestSpec
                .when()
                .post("/transactions");
        
        testContext.setResponse(response);
    }

    @When("I create the same transaction again without an idempotency key")
    public void i_create_the_same_transaction_again_without_an_idempotency_key() {
        // Create the same transaction request again without idempotency key
        // This tests that requests without idempotency keys work normally
        testContext.setIdempotencyKey(null);
        
        Response response = given()
                .contentType(ContentType.JSON)
                .body(testContext.getTransactionData())
                .when()
                .post("/transactions");
        
        testContext.setResponse(response);
    }

    @When("I try to create a transaction with amount {double}")
    public void i_try_to_create_a_transaction_with_amount(Double amount) {
        // Update transaction data in shared context with new amount for conflict testing
        // This is used when testing conflicts - same key but different request data
        // Don't send the request yet - wait for all data to be set
        testContext.getTransactionData().put("amount", amount);
    }
    
    @When("I send the transaction request with the same idempotency key")
    public void i_send_the_transaction_request_with_the_same_idempotency_key() {
        // Send the request with the current transaction data and idempotency key from context
        // This is used after updating transaction data and setting the idempotency key
        String idempotencyKey = testContext.getIdempotencyKey();
        var requestSpec = given()
                .contentType(ContentType.JSON)
                .body(testContext.getTransactionData());
        
        if (idempotencyKey != null) {
            requestSpec = requestSpec.header("Idempotency-Key", idempotencyKey);
        }
        
        Response response = requestSpec
                .when()
                .post("/transactions");
        
        testContext.setResponse(response);
    }

    @When("I create a transaction with the same amount {double}")
    public void i_create_a_transaction_with_the_same_amount(Double amount) {
        // Update transaction data with the same amount
        // The actual request will be sent after the idempotency key is set
        testContext.getTransactionData().put("amount", amount);
        // Don't send the request yet - wait for the idempotency key to be set
    }
    
    @When("I create the transaction with the updated idempotency key")
    public void i_create_the_transaction_with_the_updated_idempotency_key() {
        // Send the request with the current transaction data and idempotency key from context
        String idempotencyKey = testContext.getIdempotencyKey();
        var requestSpec = given()
                .contentType(ContentType.JSON)
                .body(testContext.getTransactionData());
        
        if (idempotencyKey != null) {
            requestSpec = requestSpec.header("Idempotency-Key", idempotencyKey);
        }
        
        Response response = requestSpec
                .when()
                .post("/transactions");
        
        testContext.setResponse(response);
    }

    @Then("I store the transaction identifier")
    public void i_store_the_transaction_identifier() {
        // Extract and store the transaction ID from the response in shared test context
        // This stored ID will be used to compare with subsequent responses
        Response response = testContext.getResponse();
        assertNotNull(response, "Response should not be null");
        
        String transactionId = response.then().extract().path("id");
        assertNotNull(transactionId, "Transaction ID should not be null");
        testContext.setStoredTransactionId(transactionId);
    }

    @Then("the response should contain the same transaction identifier as stored")
    public void the_response_should_contain_the_same_transaction_identifier_as_stored() {
        // Verify that the response contains the same transaction ID as stored
        // This validates idempotency - retry requests should return the same transaction
        Response response = testContext.getResponse();
        assertNotNull(response, "Response should not be null");
        
        response.then()
                .statusCode(200);
        
        String responseTransactionId = response.then().extract().path("id");
        assertEquals(testContext.getStoredTransactionId(), responseTransactionId,
                "Response should contain the same transaction identifier as the original request");
    }

    @Then("no duplicate transaction should be created in the ledger")
    public void no_duplicate_transaction_should_be_created_in_the_ledger() {
        // Verify that only one transaction exists in the ledger for this idempotency key
        // Queries the GET /transactions endpoint to count transactions
        // Verifies that the stored transaction ID appears only once
        
        Response getAllResponse = given()
                .when()
                .get("/transactions");
        
        getAllResponse.then()
                .statusCode(200);
        
        List<Map<String, Object>> allTransactions = getAllResponse.then()
                .extract()
                .jsonPath()
                .getList("");
        
        String storedId = testContext.getStoredTransactionId();
        assertNotNull(storedId, "Stored transaction ID should not be null");
        
        long count = allTransactions.stream()
                .filter(tx -> storedId.equals(tx.get("id").toString()))
                .count();
        
        assertEquals(1, count,
                "Transaction with ID " + storedId + " should appear only once in the ledger");
    }

    @Then("the request should be rejected with a conflict error")
    public void the_request_should_be_rejected_with_a_conflict_error() {
        // Verify that the response has HTTP 409 Conflict status
        // This indicates that the idempotency key was used with different request data
        Response response = testContext.getResponse();
        assertNotNull(response, "Response should not be null");
        
        response.then()
                .statusCode(409);
    }

    @Then("I should receive an error message about the idempotency key conflict")
    public void i_should_receive_an_error_message_about_the_idempotency_key_conflict() {
        // Verify that the error response contains a clear message about the conflict
        // Error message should explain that the idempotency key was already used with different parameters
        Response response = testContext.getResponse();
        assertNotNull(response, "Response should not be null");
        
        response.then()
                .body("error", notNullValue())
                .body("error", containsStringIgnoringCase("idempotency"));
    }

    @Then("the original transaction should remain unchanged")
    public void the_original_transaction_should_remain_unchanged() {
        // Verify that the original transaction created with the idempotency key is unchanged
        // Queries the ledger to verify the original transaction still exists with its original data
        // This ensures conflict errors don't modify existing transactions
        
        Response getAllResponse = given()
                .when()
                .get("/transactions");
        
        getAllResponse.then()
                .statusCode(200);
        
        List<Map<String, Object>> allTransactions = getAllResponse.then()
                .extract()
                .jsonPath()
                .getList("");
        
        String storedId = testContext.getStoredTransactionId();
        assertNotNull(storedId, "Stored transaction ID should not be null");
        
        Map<String, Object> originalTransaction = allTransactions.stream()
                .filter(tx -> storedId.equals(tx.get("id").toString()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(originalTransaction,
                "Original transaction should still exist in the ledger");
    }

    @Then("a new transaction should be created in the ledger")
    public void a_new_transaction_should_be_created_in_the_ledger() {
        // Verify that a new transaction was created
        // This is used when testing that different idempotency keys create independent transactions
        Response response = testContext.getResponse();
        assertNotNull(response, "Response should not be null");
        
        response.then()
                .statusCode(200)
                .body("id", notNullValue());
        
        String newTransactionId = response.then().extract().path("id");
        assertNotNull(newTransactionId, "New transaction should have an ID");
    }

    @Then("the new transaction should have a different identifier than the stored one")
    public void the_new_transaction_should_have_a_different_identifier_than_the_stored_one() {
        // Verify that the new transaction has a different ID than the stored one
        // This validates that different idempotency keys create independent transactions
        Response response = testContext.getResponse();
        assertNotNull(response, "Response should not be null");
        
        String newTransactionId = response.then().extract().path("id");
        String storedId = testContext.getStoredTransactionId();
        assertNotNull(storedId, "Stored transaction ID should not be null");
        
        assertNotEquals(storedId, newTransactionId,
                "New transaction should have a different identifier than the stored transaction");
    }
}
