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
        "feature.create-transaction.enabled=false",
        "feature.get-all-transactions.enabled=false"
})
class TransactionFeatureFlagDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn403WhenFeatureFlagIsDisabled() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content("""
                        {
                          "amount": 100.00,
                          "description": "Blocked by feature flag",
                          "type": "EXPENSE"
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn403WhenGetAllTransactionsFeatureFlagIsDisabled() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isForbidden());
    }
}
