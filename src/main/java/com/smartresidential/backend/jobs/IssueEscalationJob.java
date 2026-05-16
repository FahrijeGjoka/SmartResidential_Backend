package com.smartresidential.backend.jobs;

import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.services.interfaces.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class IssueEscalationJob {

    private static final String JOB_NAME = "IssueEscalationJob";

    private final IssueRepository issueRepository;
    private final JobService jobService;

    @Scheduled(cron = "0 0 */6 * * *")
    public void escalateOldOpenIssues() {
        jobService.runScheduledJob(JOB_NAME, this::executeNow);
    }

    public void executeNow() {
        LocalDateTime limitTime = LocalDateTime.now().minusHours(48);

        List<Issue> oldIssues = issueRepository.findByStatusInAndCreatedAtBefore(
                List.of("OPEN", "ASSIGNED", "IN_PROGRESS"),
                limitTime
        );

        for (Issue issue : oldIssues) {
            log.warn("Background job: Issue needs escalation. Issue ID: {}, Status: {}, Created at: {}",
                    issue.getId(),
                    issue.getStatus(),
                    issue.getCreatedAt()
            );

            // Këtu më vonë mund ta bëjmë:
            // 1. krijo notification për admin/staff
            // 2. ndrysho priority në HIGH
            // 3. dërgo email
        }
    }
}
