package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.VerificationToken;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends BaseRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    boolean existsByToken(String token);

    Optional<VerificationToken> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByExpiryDateBefore(LocalDateTime now);

    void deleteByUsedTrue();
}