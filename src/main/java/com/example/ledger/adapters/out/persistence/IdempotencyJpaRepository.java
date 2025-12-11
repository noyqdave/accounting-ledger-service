package com.example.ledger.adapters.out.persistence;

import com.example.ledger.adapters.out.persistence.entity.IdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IdempotencyJpaRepository extends JpaRepository<IdempotencyEntity, Long> {
    
    Optional<IdempotencyEntity> findByIdempotencyKeyAndRequestHash(String idempotencyKey, String requestHash);
    
    boolean existsByIdempotencyKeyAndRequestHashNot(String idempotencyKey, String requestHash);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM IdempotencyEntity e WHERE e.expiresAt < :now")
    void deleteByExpiresAtBefore(LocalDateTime now);
}



