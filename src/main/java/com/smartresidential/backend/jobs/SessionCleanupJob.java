package com.smartresidential.backend.jobs;

import com.smartresidential.backend.repositories.SessionRepository;
import com.smartresidential.backend.services.interfaces.JobService;
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

    private static final String JOB_NAME = "SessionCleanupJob";

    private final SessionRepository sessionRepository;
    private final JobService jobService;

    @Scheduled(cron = "0 30 2 * * *")
    @Transactional
    public void deleteExpiredSessions() {
        jobService.runScheduledJob(JOB_NAME, this::executeNow);
    }

    @Transactional
    public void executeNow() {
        LocalDateTime now = LocalDateTime.now();

        sessionRepository.deleteByExpiresAtBefore(now);

        log.info("Background job completed: expired sessions cleaned up.");
    }
}
