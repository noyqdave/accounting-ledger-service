package com.example.ledger.adapters.out.persistence;

import com.example.ledger.adapters.out.persistence.entity.IdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyJpaRepository extends JpaRepository<IdempotencyEntity, Long> {
    
    Optional<IdempotencyEntity> findByIdempotencyKeyAndRequestHash(String idempotencyKey, String requestHash);
    
    boolean existsByIdempotencyKeyAndRequestHashNot(String idempotencyKey, String requestHash);
}
