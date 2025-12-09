package com.example.ledger.adapters.in.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for idempotency metrics.
 * 
 * These tests verify that idempotency operations track metrics correctly:
 * - idempotency.requests.total: Total requests with idempotency keys
 * - idempotency.cache.hits: Requests that returned cached responses
 * - idempotency.conflicts: Requests with same key but different body
 * 
 * Uses the same testing pattern as TransactionMetricsIntegrationTest:
 * 1. Trigger the endpoint with appropriate headers/body
 * 2. Query the actuator metrics endpoint
 * 3. Assert the metric value
 * 
 * Uses JUnit 4 to match Surefire configuration for Cucumber compatibility.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "feature.create-transaction.enabled=true",
        "feature.get-all-transactions.enabled=true"
})
public class IdempotencyMetricsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private double initialRequestsTotal;
    private double initialCacheHits;

    @Before
    public void setUp() throws Exception {
        // Capture initial metric values before each test to handle metric accumulation
        // Micrometer counters are cumulative and cannot be easily reset, so we track
        // the initial value and assert on the increment
        initialRequestsTotal = getMetricValue("idempotency.requests.total");
        initialCacheHits = getMetricValue("idempotency.cache.hits");
    }

    private double getMetricValue(String metricName) throws Exception {
        try {
            MvcResult result = mockMvc.perform(get("/actuator/metrics/" + metricName))
                    .andReturn();
            
            // If metric doesn't exist, return 0.0
            if (result.getResponse().getStatus() != 200) {
                return 0.0;
            }
            
            String responseContent = result.getResponse().getContentAsString();
            return JsonPath.read(responseContent, "$.measurements[0].value");
        } catch (Exception e) {
            // Metric doesn't exist yet, return 0.0
            return 0.0;
        }
    }

    /**
     * Test: First request with idempotency key should increment idempotency.requests.total counter.
     * 
     * This is the first test in TDD - it should fail because metrics aren't implemented yet.
     * After implementing metrics, this test should pass.
     */
    @Test
    public void shouldIncrementRequestsTotalMetricWhenIdempotencyKeyIsPresent() throws Exception {
        // Arrange
        String idempotencyKey = "880e8400-e29b-41d4-a716-446655440003";

        // Act - Send POST request with idempotency key header
        mockMvc.perform(post("/transactions")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType("application/json")
                        .content("""
                        {
                          "amount": 100.00,
                          "description": "Office supplies",
                          "type": "EXPENSE"
                        }
                        """))
                .andExpect(status().isOk());

        // Assert - Verify idempotency.requests.total metric was incremented by 1
        mockMvc.perform(get("/actuator/metrics/idempotency.requests.total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements[0].value").value(initialRequestsTotal + 1.0));
    }

    /**
     * Test: Retry request with same idempotency key should increment idempotency.cache.hits counter.
     * 
     * This is the second test in TDD - it should fail because cache hit metrics aren't implemented yet.
     * After implementing metrics, this test should pass.
     */
    @Test
    public void shouldIncrementCacheHitsMetricWhenCachedResponseIsReturned() throws Exception {
        // Arrange - First request creates and caches the response
        String idempotencyKey = "880e8400-e29b-41d4-a716-446655440004";
        String requestBody = """
                {
                  "amount": 200.00,
                  "description": "Office equipment",
                  "type": "EXPENSE"
                }
                """;

        // First request - creates transaction and caches response
        mockMvc.perform(post("/transactions")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk());

        // Act - Retry request with same idempotency key and same body (should return cached response)
        mockMvc.perform(post("/transactions")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk());

        // Assert - Verify idempotency.cache.hits metric was incremented by 1
        mockMvc.perform(get("/actuator/metrics/idempotency.cache.hits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements[0].value").value(initialCacheHits + 1.0));
    }
}
