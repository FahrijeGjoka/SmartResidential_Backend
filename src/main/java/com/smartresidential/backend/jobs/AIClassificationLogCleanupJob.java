package com.smartresidential.backend.jobs;

import com.smartresidential.backend.repositories.AIClassificationLogRepository;
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
public class AIClassificationLogCleanupJob {

    private static final String JOB_NAME = "AIClassificationLogCleanupJob";

    private final AIClassificationLogRepository aiClassificationLogRepository;
    private final JobService jobService;

    @Scheduled(cron = "0 30 4 * * *")
    @Transactional
    public void deleteOldAIClassificationLogs() {
        jobService.runScheduledJob(JOB_NAME, this::executeNow);
    }

    @Transactional
    public void executeNow() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(90);

        aiClassificationLogRepository.deleteByCreatedAtBefore(limitDate);

        log.info("Background job completed: AI classification logs older than 90 days deleted.");
    }
}
