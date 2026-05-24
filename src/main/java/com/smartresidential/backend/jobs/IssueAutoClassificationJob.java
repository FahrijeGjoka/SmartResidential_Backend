package com.smartresidential.backend.jobs;

import com.smartresidential.backend.ai.IssueCategoryMatch;
import com.smartresidential.backend.cache.CacheNames;
import com.smartresidential.backend.cache.TenantCacheEvictor;
import com.smartresidential.backend.entities.AIClassificationStatus;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.IssueAssignment;
import com.smartresidential.backend.entities.IssueCategory;
import com.smartresidential.backend.entities.MaintenanceRequest;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.TechnicianProfile;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.repositories.IssueAssignmentRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.IssueCategoryRepository;
import com.smartresidential.backend.repositories.MaintenanceRequestRepository;
import com.smartresidential.backend.repositories.RoleRepository;
import com.smartresidential.backend.repositories.TechnicianProfileRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.IssueCategoryMatcherService;
import com.smartresidential.backend.services.interfaces.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueAutoClassificationJob {

    private static final String STATUS_ASSIGNED = "ASSIGNED";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String ROLE_TECHNICIAN = "ROLE_TECHNICIAN";
    private static final String GENERAL_MAINTENANCE_SPECIALIZATION = "General maintenance";
    private static final String GENERAL_MAINTENANCE_CATEGORY = "General maintenance";
    private static final int DEFAULT_MAX_ACTIVE_ISSUES = 5;
    private static final Set<String> ACTIVE_WORKLOAD_STATUSES = Set.of(STATUS_ASSIGNED, STATUS_IN_PROGRESS);
    private static final Set<String> HIGH_PRIORITY_VALUES = Set.of("URGENT", "HIGH");

    private final IssueRepository issueRepository;
    private final IssueAssignmentRepository issueAssignmentRepository;
        private final IssueCategoryRepository issueCategoryRepository;
private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final TechnicianProfileRepository technicianProfileRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final IssueCategoryMatcherService issueCategoryMatcherService;
    private final NotificationJob notificationJob;
    private final TenantCacheEvictor tenantCacheEvictor;
    private final AuditLogService auditLogService;

    @Async
    @Transactional
    public void classifyAndAssign(Long issueId, Long requestedByUserId) {
        try {
            Issue issue = issueRepository.findById(issueId)
                    .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

            if (Boolean.TRUE.equals(issue.getArchived()) || issue.getCategory() != null) {
                return;
            }

            issue.setAiClassificationStatus(AIClassificationStatus.PROCESSING);
            issueRepository.save(issue);

            IssueCategoryMatch categoryMatch = issueCategoryMatcherService
                    .matchCategory(issue.getTitle(), issue.getDescription())
                    .orElse(null);

            if (categoryMatch == null) {
                IssueCategory fallbackCategory = issueCategoryRepository.findByName(GENERAL_MAINTENANCE_CATEGORY)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Fallback issue category not found: " + GENERAL_MAINTENANCE_CATEGORY));

                issue.setCategory(fallbackCategory);
                issue.setAiClassificationStatus(AIClassificationStatus.COMPLETED);
                issue.setAiCategoryConfidence(null);
                issue.setAiCategoryReason("No confident AI category matched; assigned fallback category: " + GENERAL_MAINTENANCE_CATEGORY + ".");

                Issue savedIssue = issueRepository.save(issue);
                Issue assignedIssue = autoAssignTechnician(savedIssue, resolveRequester(requestedByUserId));
                tenantCacheEvictor.evictCurrentTenant(CacheNames.ISSUES);
                auditLogService.logCurrentUser("AI_CLASSIFICATION_FALLBACK", "ISSUE", assignedIssue.getId());
                log.info("Background job completed: issue {} assigned fallback category {}",
                        assignedIssue.getId(),
                        GENERAL_MAINTENANCE_CATEGORY);
                return;
            }

            issue.setCategory(categoryMatch.getCategory());
            issue.setAiCategoryConfidence(categoryMatch.getConfidence());
            issue.setAiCategoryReason(categoryMatch.getReason());
            issue.setAiClassificationStatus(AIClassificationStatus.COMPLETED);

            Issue savedIssue = issueRepository.save(issue);
            Issue assignedIssue = autoAssignTechnician(savedIssue, resolveRequester(requestedByUserId));
            tenantCacheEvictor.evictCurrentTenant(CacheNames.ISSUES);
            auditLogService.logCurrentUser("AI_CLASSIFICATION_COMPLETED", "ISSUE", assignedIssue.getId());

            log.info("Background job completed: issue {} auto-classified as {}",
                    assignedIssue.getId(),
                    categoryMatch.getCategory().getName());
        } catch (RuntimeException exception) {
            issueRepository.findById(issueId).ifPresent(issue -> {
                if (!Boolean.TRUE.equals(issue.getArchived()) && issue.getCategory() == null) {
                    issue.setAiClassificationStatus(AIClassificationStatus.FAILED);
                    issue.setAiCategoryReason(exception.getMessage());
                    issueRepository.save(issue);
                    tenantCacheEvictor.evictCurrentTenant(CacheNames.ISSUES);
                    auditLogService.logCurrentUser("AI_CLASSIFICATION_FAILED", "ISSUE", issueId);
                }
            });
            log.warn("Background job failed: issue auto-classification for issue {}: {}",
                    issueId,
                    exception.getMessage(),
                    exception);
        }
    }

    private Issue autoAssignTechnician(Issue issue, User requestedBy) {
        var category = issue.getCategory();
        if (category == null || category.getRequiredSpecialization() == null
                || category.getRequiredSpecialization().isBlank()) {
            return issue;
        }

        Optional<TechnicianProfile> selectedTechnician =
                selectTechnician(category.getRequiredSpecialization(), issue.getPriority());

        if (selectedTechnician.isEmpty()) {
            return issue;
        }

        User technician = selectedTechnician.get().getUser();
        IssueAssignment assignment = new IssueAssignment();
        assignment.setIssue(issue);
        assignment.setTechnician(technician);
        issueAssignmentRepository.save(assignment);

        issue.setStatus(STATUS_ASSIGNED);
        Issue updatedIssue = issueRepository.save(issue);
        ensureMaintenanceWorkOrder(updatedIssue, requestedBy);
        notificationJob.notifyTechnicianAssigned(updatedIssue.getId(), technician.getId());
        return updatedIssue;
    }

    private User resolveRequester(Long requestedByUserId) {
        if (requestedByUserId == null) {
            return null;
        }

        return userRepository.findById(requestedByUserId).orElse(null);
    }

    private void ensureMaintenanceWorkOrder(Issue issue, User requestedBy) {
        if (issue == null || issue.getId() == null || requestedBy == null) {
            return;
        }

        if (maintenanceRequestRepository.existsByIssue_Id(issue.getId())) {
            return;
        }

        MaintenanceRequest maintenanceRequest = new MaintenanceRequest();
        maintenanceRequest.setIssue(issue);
        maintenanceRequest.setRequestedBy(requestedBy);
        maintenanceRequest.setDescription("Work order for issue: " + nullToBlank(issue.getTitle()));
        maintenanceRequestRepository.save(maintenanceRequest);
    }

    private Optional<TechnicianProfile> selectTechnician(String requiredSpecialization, String priority) {
        List<TechnicianProfile> matchingSpecialists = findEligibleTechnicians(requiredSpecialization);
        if (!matchingSpecialists.isEmpty()) {
            return selectLowestWorkload(matchingSpecialists, priority);
        }

        return selectLowestWorkload(
                findEligibleTechnicians(GENERAL_MAINTENANCE_SPECIALIZATION),
                priority
        );
    }

    private List<TechnicianProfile> findEligibleTechnicians(String specialization) {
        String normalizedSpecialization = normalizeSpecialization(specialization);
        return technicianProfileRepository.findByIsAvailableTrue()
                .stream()
                .filter(profile -> normalizedSpecialization.equals(normalizeSpecialization(profile.getSpecialization())))
                .filter(this::isTechnicianProfileAssignable)
                .filter(profile -> activeWorkload(profile, false) < maxActiveIssues(profile))
                .toList();
    }

    private Optional<TechnicianProfile> selectLowestWorkload(List<TechnicianProfile> profiles, String priority) {
        boolean highPriority = isHighPriority(priority);
        return profiles.stream()
                .min(Comparator
                        .comparingInt((TechnicianProfile profile) -> highPriority
                                ? activeWorkload(profile, true)
                                : activeWorkload(profile, false))
                        .thenComparing(profile -> latestAssignedAt(profile), Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(profile -> profile.getUser().getId()));
    }

    private int activeWorkload(TechnicianProfile profile, boolean highPriorityOnly) {
        return (int) issueAssignmentRepository.findByTechnicianId(profile.getUser().getId())
                .stream()
                .map(IssueAssignment::getIssue)
                .filter(assignedIssue -> assignedIssue != null
                        && !Boolean.TRUE.equals(assignedIssue.getArchived())
                        && ACTIVE_WORKLOAD_STATUSES.contains(assignedIssue.getStatus()))
                .filter(assignedIssue -> !highPriorityOnly || isHighPriority(assignedIssue.getPriority()))
                .count();
    }

    private LocalDateTime latestAssignedAt(TechnicianProfile profile) {
        return issueAssignmentRepository.findByTechnicianId(profile.getUser().getId())
                .stream()
                .map(IssueAssignment::getAssignedAt)
                .filter(assignedAt -> assignedAt != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private int maxActiveIssues(TechnicianProfile profile) {
        return profile.getMaxActiveIssues() == null || profile.getMaxActiveIssues() < 1
                ? DEFAULT_MAX_ACTIVE_ISSUES
                : profile.getMaxActiveIssues();
    }

    private boolean isTechnicianProfileAssignable(TechnicianProfile profile) {
        User user = profile.getUser();
        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            return false;
        }

        return roleRepository.findById(user.getRoleId())
                .map(Role::getName)
                .filter(ROLE_TECHNICIAN::equals)
                .isPresent();
    }

    private boolean isHighPriority(String priority) {
        return priority != null && HIGH_PRIORITY_VALUES.contains(priority.toUpperCase(Locale.ROOT));
    }

    private String normalizeSpecialization(String specialization) {
        return specialization == null ? "" : specialization.trim().toLowerCase(Locale.ROOT);
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
