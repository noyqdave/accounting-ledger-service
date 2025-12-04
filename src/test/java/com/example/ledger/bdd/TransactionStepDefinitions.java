package com.example.ledger.bdd;

import com.example.ledger.adapters.out.persistence.TransactionJpaRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = {
    "feature.create-transaction.enabled=true",
    "feature.get-all-transactions.enabled=true"
})
public class TransactionStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private TransactionJpaRepository transactionJpaRepository;

    @Autowired
    private TestContext testContext;

    private Response response;
    private Map<String, Object> transactionData = new HashMap<>();
    private String baseUrl;

    @Before
    public void setUp() {
        // Clean the database before each scenario
        transactionJpaRepository.deleteAll();
        transactionData.clear();
        // Also clear shared test context
        if (testContext != null) {
            testContext.clear();
        }
    }

    @Given("I want to record a transaction")
    public void i_want_to_record_a_transaction() {
        baseUrl = "http://localhost:" + port;
        RestAssured.baseURI = baseUrl;
    }

    @Given("the transaction amount is {double}")
    public void the_transaction_amount_is(Double amount) {
        transactionData.put("amount", amount);
        // Also update shared context for idempotency tests
        testContext.getTransactionData().put("amount", amount);
    }

    @Given("the transaction description is {string}")
    public void the_transaction_description_is(String description) {
        if (!"null".equals(description)) {
            transactionData.put("description", description);
            // Also update shared context for idempotency tests
            testContext.getTransactionData().put("description", description);
        }
    }

    @Given("the transaction type is {string}")
    public void the_transaction_type_is(String type) {
        transactionData.put("type", type);
        // Also update shared context for idempotency tests
        testContext.getTransactionData().put("type", type);
    }

    @When("I create the transaction")
    public void i_create_the_transaction() {
        // Build request - optionally include idempotency key header if set
        var requestSpec = given()
                .contentType(ContentType.JSON)
                .body(transactionData);
        
        // Include Idempotency-Key header if set in test context
        // This allows idempotency scenarios to work with existing transaction creation steps
        String idempotencyKey = testContext.getIdempotencyKey();
        if (idempotencyKey != null) {
            requestSpec = requestSpec.header("Idempotency-Key", idempotencyKey);
        }
        
        response = requestSpec
                .when()
                .post("/transactions");
        
        // Store response in shared context for idempotency step definitions
        testContext.setResponse(response);
    }

    @Then("the transaction should be recorded in the ledger")
    public void the_transaction_should_be_recorded_in_the_ledger() {
        // First verify the HTTP response is successful
        response.then()
                .statusCode(200)
                .body("id", notNullValue());
        
        // Extract the transaction ID from the response
        String transactionIdString = response.then().extract().path("id");
        assertNotNull(transactionIdString, "Transaction ID should not be null");
        UUID transactionId = UUID.fromString(transactionIdString);
        
        // Verify the transaction exists in the database by retrieving all transactions
        Response getAllResponse = given()
                .when()
                .get("/transactions");
        
        getAllResponse.then()
                .statusCode(200);
        
        // Find the transaction in the list by ID
        List<Map<String, Object>> allTransactions = getAllResponse.then()
                .extract()
                .jsonPath()
                .getList("");
        
        Map<String, Object> savedTransaction = allTransactions.stream()
                .filter(tx -> transactionIdString.equals(tx.get("id").toString()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(savedTransaction, 
                "Transaction with ID " + transactionId + " should exist in the ledger");
        
        // Verify the transaction data in the ledger matches what was sent
        Number expectedAmount = ((Number) transactionData.get("amount")).floatValue();
        assertEquals(expectedAmount.floatValue(), 
                ((Number) savedTransaction.get("amount")).floatValue(),
                "Transaction amount in ledger should match the sent amount");
        assertEquals(transactionData.get("description"), savedTransaction.get("description"),
                "Transaction description in ledger should match the sent description");
        assertEquals(transactionData.get("type"), savedTransaction.get("type").toString(),
                "Transaction type in ledger should match the sent type");
        assertNotNull(savedTransaction.get("date"), "Transaction date should not be null");
    }

    @Then("the transaction should have a unique identifier")
    public void the_transaction_should_have_a_unique_identifier() {
        String id = response.then().extract().path("id");
        assertNotNull(id);
        assertDoesNotThrow(() -> UUID.fromString(id), "ID should be a valid UUID");
    }

    @Then("the transaction should show the correct amount of {double}")
    public void the_transaction_should_show_the_correct_amount_of(Double expectedAmount) {
        response.then()
                .body("amount", equalTo(expectedAmount.floatValue()));
    }

    @Then("the transaction should show the description {string}")
    public void the_transaction_should_show_the_description(String expectedDescription) {
        response.then()
                .body("description", equalTo(expectedDescription));
    }

    @Then("^the transaction should be marked as (.+)$")
    public void the_transaction_should_be_marked_as(String expectedType) {
        response.then()
                .body("type", equalTo(expectedType));
    }

    @Then("the transaction should have a timestamp showing when it was created")
    public void the_transaction_should_have_a_timestamp_showing_when_it_was_created() {
        response.then()
                .body("date", notNullValue());
    }

    @Then("the transaction should not be created")
    public void the_transaction_should_not_be_created() {
        response.then()
                .statusCode(anyOf(equalTo(400), equalTo(403), equalTo(422)));
    }

    @Then("I should receive an error message about the amount being invalid")
    public void i_should_receive_an_error_message_about_the_amount_being_invalid() {
        response.then()
                .body("error", containsString("Amount must be positive"));
    }

    @Then("I should receive an error message about the description being required")
    public void i_should_receive_an_error_message_about_the_description_being_required() {
        response.then()
                .body("error", containsString("Description must not be null or empty"));
    }

    @Then("I should receive an error message about the transaction type being invalid")
    public void i_should_receive_an_error_message_about_the_transaction_type_being_invalid() {
        response.then()
                .body("error", anyOf(
                    containsString("type"),
                    containsString("invalid")
                ));
    }

    @Given("the transaction description is null")
    public void the_transaction_description_is_null() {
        // Don't add description to transactionData, it will be null
    }
}
