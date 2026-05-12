package com.smartresidential.backend.jobs;

import com.smartresidential.backend.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogCleanupJob {

    private final AuditLogRepository auditLogRepository;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void deleteOldAuditLogs() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(180);

        auditLogRepository.deleteByCreatedAtBefore(limitDate);

        log.info("Background job completed: audit logs older than 180 days deleted.");
    }
}