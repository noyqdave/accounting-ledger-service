package com.example.ledger.adapters.out.persistence.entity;

import com.example.ledger.domain.model.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    private UUID id;

    private LocalDateTime date;
    private BigDecimal amount;
    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    // Getters and setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
}
