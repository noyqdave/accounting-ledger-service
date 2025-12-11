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
 * Integration tests for transaction metrics.
 * 
 * These tests verify that transaction operations track metrics correctly:
 * - transactions.created: Counter for created transactions
 * - transactions.fetched: Counter for retrieved transactions
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
public class TransactionMetricsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private double initialTransactionsCreated;
    private double initialTransactionsFetched;

    @Before
    public void setUp() throws Exception {
        // Capture initial metric values before each test to handle metric accumulation
        // Micrometer counters are cumulative and cannot be easily reset, so we track
        // the initial value and assert on the increment
        initialTransactionsCreated = getMetricValue("transactions.created");
        initialTransactionsFetched = getMetricValue("transactions.fetched");
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

    @Test
    public void shouldIncrementMetricWhenTransactionIsCreated() throws Exception {
        // 1. Trigger metric by calling the controller
        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content("""
                        {
                          "amount": 100.00,
                          "description": "Track me!",
                          "type": "EXPENSE"
                        }
                        """))
                .andExpect(status().isOk());

        // 2. Query the metrics endpoint
        mockMvc.perform(get("/actuator/metrics/transactions.created"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements[0].value").value(initialTransactionsCreated + 1.0));
    }

    @Test
    public void shouldIncrementMetricWhenTransactionsAreFetched() throws Exception {
        // 1. Trigger metric by calling the controller
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk());

        // 2. Query the metrics endpoint
        mockMvc.perform(get("/actuator/metrics/transactions.fetched"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements[0].value").value(initialTransactionsFetched + 1.0));
    }
}