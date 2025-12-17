package com.example.ledger.bdd.steps;

import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared test context for BDD step definitions.
 * Allows step definitions to share state like transaction data, responses, and idempotency keys.
 */
@Component
public class TestContext {
    
    private Map<String, Object> transactionData = new HashMap<>();
    private Response response;
    private String idempotencyKey;
    private String storedTransactionId;
    
    public Map<String, Object> getTransactionData() {
        return transactionData;
    }
    
    public void clearTransactionData() {
        transactionData.clear();
    }
    
    public Response getResponse() {
        return response;
    }
    
    public void setResponse(Response response) {
        this.response = response;
    }
    
    public String getIdempotencyKey() {
        return idempotencyKey;
    }
    
    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
    
    public String getStoredTransactionId() {
        return storedTransactionId;
    }
    
    public void setStoredTransactionId(String storedTransactionId) {
        this.storedTransactionId = storedTransactionId;
    }
    
    public void clear() {
        transactionData.clear();
        response = null;
        idempotencyKey = null;
        storedTransactionId = null;
    }
}
