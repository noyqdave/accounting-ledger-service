package com.example.ledger.adapters.in.web;

import com.example.ledger.application.usecase.CreateTransactionUseCase;
import com.example.ledger.application.usecase.GetAllTransactionsUseCase;
import com.example.ledger.domain.model.Transaction;
import com.example.ledger.domain.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetAllTransactionsUseCase getAllTransactionsUseCase;
    @MockBean
    private CreateTransactionUseCase createTransactionUseCase;

    @Test
    void shouldReturnListOfTransactions() throws Exception {
        Transaction t1 = new Transaction(
                UUID.randomUUID(),
                LocalDateTime.now(),
                new BigDecimal("100.00"),
                "Hosting",
                TransactionType.EXPENSE
        );
        Transaction t2 = new Transaction(
                UUID.randomUUID(),
                LocalDateTime.now(),
                new BigDecimal("200.00"),
                "Client payment",
                TransactionType.REVENUE
        );

        when(getAllTransactionsUseCase.getAll()).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].description").value("Hosting"))
                .andExpect(jsonPath("$[1].type").value("REVENUE"));
    }

    @Test
    void shouldCreateTransaction() throws Exception {
        Transaction savedTransaction = new Transaction(
                UUID.randomUUID(),
                LocalDateTime.now(),
                new BigDecimal("125.00"),
                "Cloud hosting",
                TransactionType.EXPENSE
        );

        // Mock the use case to return the saved transaction
        when(createTransactionUseCase.create(any(Transaction.class)))
                .thenReturn(savedTransaction);

        String requestJson = """
        {
          "amount": 125.00,
          "description": "Cloud hosting",
          "type": "EXPENSE"
        }
        """;

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Cloud hosting"))
                .andExpect(jsonPath("$.amount").value(125.00))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }
}