package com.example.ledger.adapters.out.persistence;

import com.example.ledger.adapters.out.persistence.entity.TransactionEntity;
import com.example.ledger.domain.model.Transaction;
import com.example.ledger.domain.port.TransactionRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final TransactionJpaRepository jpaRepository;

    public TransactionRepositoryAdapter(TransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = mapToEntity(transaction);
        TransactionEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    private TransactionEntity mapToEntity(Transaction tx) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(tx.getId());
        entity.setDate(tx.getDate());
        entity.setAmount(tx.getAmount());
        entity.setDescription(tx.getDescription());
        entity.setType(tx.getType());
        return entity;
    }

    private Transaction mapToDomain(TransactionEntity e) {
        return new Transaction(
                e.getId(),
                e.getDate(),
                e.getAmount(),
                e.getDescription(),
                e.getType()
        );
    }
}