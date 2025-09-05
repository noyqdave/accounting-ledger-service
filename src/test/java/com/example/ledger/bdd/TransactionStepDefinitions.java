package com.example.ledger.bdd;

import com.example.ledger.domain.model.TransactionType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.HashMap;
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

    private Response response;
    private Map<String, Object> transactionData = new HashMap<>();
    private String baseUrl;

    @Given("I want to record a transaction")
    public void i_want_to_record_a_transaction() {
        baseUrl = "http://localhost:" + port;
        RestAssured.baseURI = baseUrl;
    }

    @And("the transaction amount is {double}")
    public void the_transaction_amount_is(Double amount) {
        transactionData.put("amount", amount);
    }

    @And("the transaction description is {string}")
    public void the_transaction_description_is(String description) {
        if (!"null".equals(description)) {
            transactionData.put("description", description);
        }
    }

    @And("the transaction type is {string}")
    public void the_transaction_type_is(String type) {
        transactionData.put("type", type);
    }

    @When("I create the transaction")
    public void i_create_the_transaction() {
        response = given()
                .contentType(ContentType.JSON)
                .body(transactionData)
                .when()
                .post("/transactions");
    }

    @Then("the transaction should be recorded in the ledger")
    public void the_transaction_should_be_recorded_in_the_ledger() {
        response.then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("amount", equalTo(((Number) transactionData.get("amount")).floatValue()))
                .body("description", equalTo(transactionData.get("description")))
                .body("type", equalTo(transactionData.get("type")))
                .body("date", notNullValue());
    }

    @And("the transaction should have a unique identifier")
    public void the_transaction_should_have_a_unique_identifier() {
        String id = response.then().extract().path("id");
        assertNotNull(id);
        assertDoesNotThrow(() -> UUID.fromString(id), "ID should be a valid UUID");
    }

    @And("the transaction should show the correct amount of {double}")
    public void the_transaction_should_show_the_correct_amount_of(Double expectedAmount) {
        response.then()
                .body("amount", equalTo(expectedAmount.floatValue()));
    }

    @And("the transaction should show the description {string}")
    public void the_transaction_should_show_the_description(String expectedDescription) {
        response.then()
                .body("description", equalTo(expectedDescription));
    }

    @And("the transaction should be marked as {string}")
    public void the_transaction_should_be_marked_as(String expectedType) {
        response.then()
                .body("type", equalTo(expectedType));
    }

    @And("the transaction should have a timestamp showing when it was created")
    public void the_transaction_should_have_a_timestamp_showing_when_it_was_created() {
        response.then()
                .body("date", notNullValue());
    }

    @Then("the transaction should not be created")
    public void the_transaction_should_not_be_created() {
        response.then()
                .statusCode(anyOf(equalTo(400), equalTo(403), equalTo(422)));
    }

    @And("I should receive an error message about the amount being invalid")
    public void i_should_receive_an_error_message_about_the_amount_being_invalid() {
        response.then()
                .body("error", containsString("Amount must be positive"));
    }

    @And("I should receive an error message about the description being required")
    public void i_should_receive_an_error_message_about_the_description_being_required() {
        response.then()
                .body("error", containsString("Description must not be null or empty"));
    }

    @And("I should receive an error message about the transaction type being invalid")
    public void i_should_receive_an_error_message_about_the_transaction_type_being_invalid() {
        response.then()
                .body("error", anyOf(
                    containsString("type"),
                    containsString("invalid"),
                    containsString("enum")
                ));
    }

    @Given("the transaction description is null")
    public void the_transaction_description_is_null() {
        // Don't add description to transactionData, it will be null
    }
}
