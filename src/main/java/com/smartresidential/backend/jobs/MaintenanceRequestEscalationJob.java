package com.smartresidential.backend.jobs;

import com.smartresidential.backend.entities.MaintenanceRequest;
import com.smartresidential.backend.repositories.MaintenanceRequestRepository;
import com.smartresidential.backend.services.interfaces.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceRequestEscalationJob {

    private static final String JOB_NAME = "MaintenanceRequestEscalationJob";

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final NotificationJob notificationJob;
    private final JobService jobService;

    @Scheduled(cron = "0 0 */8 * * *")
    @Transactional(readOnly = true)
    public void escalateOldMaintenanceRequests() {
        jobService.runScheduledJob(JOB_NAME, this::executeNow);
    }

    @Transactional(readOnly = true)
    public void executeNow() {
        LocalDateTime limitTime = LocalDateTime.now().minusHours(72);

        List<MaintenanceRequest> oldRequests =
                maintenanceRequestRepository.findByRequestedAtBefore(limitTime);

        for (MaintenanceRequest request : oldRequests) {
            notificationJob.notifyMaintenanceRequestEscalation(
                    request.getId(),
                    request.getIssue().getId()
            );

            log.warn("Background job: maintenance request needs escalation. Request ID: {}, Issue ID: {}",
                    request.getId(),
                    request.getIssue().getId()
            );
        }
    }
}
