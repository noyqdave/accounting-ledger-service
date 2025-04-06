package com.example.ledger.adapters.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "feature.create-transaction.enabled=true",
        "feature.get-all-transactions.enabled=true"
})
class TransactionMetricsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldIncrementMetricWhenTransactionIsCreated() throws Exception {
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
                .andExpect(jsonPath("$.measurements[0].value").value(1.0));
    }

    @Test
    void shouldIncrementMetricWhenTransactionsAreFetched() throws Exception {
        // 1. Trigger metric by calling the controller
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk());

        // 2. Query the metrics endpoint
        mockMvc.perform(get("/actuator/metrics/transactions.fetched"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements[0].value").value(1.0));
    }
}