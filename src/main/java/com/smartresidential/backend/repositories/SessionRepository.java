package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByToken(String token);

    Optional<Session> findByTokenAndExpiresAtAfter(String token, LocalDateTime now);

    List<Session> findAllByUserId(Long userId);
}