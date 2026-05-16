package com.smartresidential.backend.jobs;

import com.smartresidential.backend.repositories.VerificationTokenRepository;
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
public class VerificationTokenCleanupJob {

    private static final String JOB_NAME = "VerificationTokenCleanupJob";

    private final VerificationTokenRepository verificationTokenRepository;
    private final JobService jobService;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void deleteExpiredAndUsedTokens() {
        jobService.runScheduledJob(JOB_NAME, this::executeNow);
    }

    @Transactional
    public void executeNow() {
        LocalDateTime now = LocalDateTime.now();

        verificationTokenRepository.deleteByExpiryDateBefore(now);
        verificationTokenRepository.deleteByUsedTrue();

        log.info("Background job completed: expired and used verification tokens cleaned up.");
    }
}
