package com.smartresidential.backend.jobs;

import com.smartresidential.backend.repositories.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCleanupJob {

    private final SessionRepository sessionRepository;

    @Scheduled(cron = "0 30 2 * * *")
    @Transactional
    public void deleteExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();

        sessionRepository.deleteByExpiresAtBefore(now);

        log.info("Background job completed: expired sessions cleaned up.");
    }
}