package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.ai.IssueCategoryMatch;
import com.smartresidential.backend.dto.issue.CreateIssueRequest;
import com.smartresidential.backend.dto.issue.IssueFilterRequest;
import com.smartresidential.backend.dto.issue.IssueResponseDTO;
import com.smartresidential.backend.dto.issue.UpdateIssueRequest;
import com.smartresidential.backend.entities.AIClassificationStatus;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.IssueAssignment;
import com.smartresidential.backend.entities.IssueCategory;
import com.smartresidential.backend.entities.IssueStatusHistory;
import com.smartresidential.backend.entities.MaintenanceRequest;
import com.smartresidential.backend.entities.ResidentProfile;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.TechnicianProfile;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.cache.CacheNames;
import com.smartresidential.backend.cache.TenantCacheEvictor;
import com.smartresidential.backend.exceptions.BadRequestException;
import com.smartresidential.backend.exceptions.ForbiddenException;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.exceptions.UnauthorizedException;
import com.smartresidential.backend.jobs.IssueAutoClassificationJob;
import com.smartresidential.backend.jobs.NotificationJob;
import com.smartresidential.backend.mapper.IssueMapper;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.ApartmentRepository;
import com.smartresidential.backend.repositories.AIClassificationLogRepository;
import com.smartresidential.backend.repositories.AttachmentRepository;
import com.smartresidential.backend.repositories.CommentRepository;
import com.smartresidential.backend.repositories.IssueAssignmentRepository;
import com.smartresidential.backend.repositories.IssueCategoryRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.IssueStatusHistoryRepository;
import com.smartresidential.backend.repositories.MaintenanceRequestRepository;
import com.smartresidential.backend.repositories.ResidentProfileRepository;
import com.smartresidential.backend.repositories.RoleRepository;
import com.smartresidential.backend.repositories.TechnicianProfileRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.repositories.WorkLogRepository;
import com.smartresidential.backend.services.interfaces.IssueCategoryMatcherService;
import com.smartresidential.backend.services.interfaces.IssueService;
import com.smartresidential.backend.services.interfaces.AuditLogService;
import com.smartresidential.backend.specifications.IssueSpecification;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueServiceImpl implements IssueService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_ASSIGNED = "ASSIGNED";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String ROLE_RESIDENT = "ROLE_RESIDENT";
    private static final String ROLE_TECHNICIAN = "ROLE_TECHNICIAN";
    private static final String GENERAL_MAINTENANCE_SPECIALIZATION = "General maintenance";
    private static final int DEFAULT_MAX_ACTIVE_ISSUES = 5;
    private static final Set<String> ACTIVE_WORKLOAD_STATUSES = Set.of(STATUS_ASSIGNED, STATUS_IN_PROGRESS);
    private static final Set<String> HIGH_PRIORITY_VALUES = Set.of("URGENT", "HIGH");
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "title",
            "status",
            "priority",
            "createdAt",
            "updatedAt"
    );

    private final IssueRepository issueRepository;
    private final IssueCategoryRepository issueCategoryRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final IssueAssignmentRepository issueAssignmentRepository;
    private final IssueStatusHistoryRepository issueStatusHistoryRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final WorkLogRepository workLogRepository;
    private final AIClassificationLogRepository aiClassificationLogRepository;
    private final ResidentProfileRepository residentProfileRepository;
    private final TechnicianProfileRepository technicianProfileRepository;
    private final RoleRepository roleRepository;
    private final IssueCategoryMatcherService issueCategoryMatcherService;
    private final IssueAutoClassificationJob issueAutoClassificationJob;
    private final NotificationJob notificationJob;
    private final IssueMapper issueMapper;
    private final TenantCacheEvictor tenantCacheEvictor;
    private final AuditLogService auditLogService;

    @Override
    public IssueResponseDTO createIssue(CreateIssueRequest request) {
        Long loggedInUserId = TenantContext.getUserId();

        if (loggedInUserId == null) {
            throw new UnauthorizedException("Authenticated user is required.");
        }

        User createdBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + loggedInUserId
                ));

        Apartment apartment = resolveIssueApartment(request, loggedInUserId);

        IssueCategory category = null;
        boolean autoClassify = false;
        if (!isResident() && request.getCategoryId() != null) {
            category = issueCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Issue category not found with id: " + request.getCategoryId()
                    ));
        } else {
            autoClassify = true;
        }

        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setStatus(STATUS_OPEN);
        issue.setPriority(request.getPriority());
        issue.setApartment(apartment);
        issue.setCreatedBy(createdBy);
        issue.setCategory(category);
        issue.setAiClassificationStatus(autoClassify
                ? AIClassificationStatus.PENDING
                : AIClassificationStatus.COMPLETED);

        Issue savedIssue = issueRepository.save(issue);
        Issue assignedIssue = autoClassify ? savedIssue : autoAssignTechnician(savedIssue, createdBy);
        evictCurrentTenantIssueCache();
        scheduleAfterCommit(() -> notificationJob.notifyIssueCreated(assignedIssue.getId()));
        auditLogService.logCurrentUser("ISSUE_CREATED", "ISSUE", assignedIssue.getId());
        if (autoClassify) {
            scheduleAutoClassification(assignedIssue.getId(), createdBy.getId());
        }
        return mapToResponse(assignedIssue);
    }

    @Override
    public IssueResponseDTO updateIssue(Long id, UpdateIssueRequest request) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));

        if (request.getTitle() != null) {
            issue.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            issue.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            updateIssueStatus(issue, request.getStatus());
        }

        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }

        if (request.getCategoryId() != null) {
            IssueCategory category = issueCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Issue category not found with id: " + request.getCategoryId()
                    ));
            issue.setCategory(category);
            issue.setAiClassificationStatus(AIClassificationStatus.COMPLETED);
        }

        Issue updatedIssue = issueRepository.save(issue);
        evictCurrentTenantIssueCache();
        auditLogService.logCurrentUser("ISSUE_UPDATED", "ISSUE", updatedIssue.getId());
        return mapToResponse(updatedIssue);
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponseDTO getIssueById(Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));

        return mapToResponse(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponseDTO> getAllIssues() {
        if (isResident()) {
            return getMyIssues();
        }

        return issueRepository.findByArchivedFalse()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponseDTO> getMyIssues() {
        Long currentUserId = requireAuthenticatedUserId();

        return issueRepository.findByCreatedByIdAndArchivedFalse(currentUserId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUES,
            key = "T(com.smartresidential.backend.cache.IssueCacheKeys).search(#filter)"
    )
    public Page<IssueResponseDTO> searchIssues(IssueFilterRequest filter) {
        return issueRepository.findAll(IssueSpecification.withFilters(filter), buildPageRequest(filter))
                .map(this::mapToResponse);
    }

    @Override
    public void deleteIssue(Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));

        Long issueId = issue.getId();
        maintenanceRequestRepository.deleteByIssue_Id(issueId);
        issueAssignmentRepository.deleteByIssueId(issueId);
        issueStatusHistoryRepository.deleteByIssueId(issueId);
        commentRepository.deleteByIssueId(issueId);
        attachmentRepository.deleteByIssueId(issueId);
        workLogRepository.deleteByIssueId(issueId);
        aiClassificationLogRepository.deleteByIssueId(issueId);
        issueRepository.delete(issue);
        evictCurrentTenantIssueCache();
        auditLogService.logCurrentUser("ISSUE_DELETED", "ISSUE", issueId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUES,
            key = "T(com.smartresidential.backend.cache.IssueCacheKeys).byStatus(#status)",
            condition = "T(com.smartresidential.backend.multitenancy.TenantContext).getRoleName() != 'ROLE_RESIDENT'"
    )
    public List<IssueResponseDTO> getIssuesByStatus(String status) {
        if (isResident()) {
            return getMyIssues();
        }

        return issueRepository.findByStatusAndArchivedFalse(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUES,
            key = "T(com.smartresidential.backend.cache.IssueCacheKeys).byPriority(#priority)",
            condition = "T(com.smartresidential.backend.multitenancy.TenantContext).getRoleName() != 'ROLE_RESIDENT'"
    )
    public List<IssueResponseDTO> getIssuesByPriority(String priority) {
        if (isResident()) {
            return getMyIssues();
        }

        return issueRepository.findByPriorityAndArchivedFalse(priority)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUES,
            key = "T(com.smartresidential.backend.cache.IssueCacheKeys).byCategory(#categoryId)",
            condition = "T(com.smartresidential.backend.multitenancy.TenantContext).getRoleName() != 'ROLE_RESIDENT'"
    )
    public List<IssueResponseDTO> getIssuesByCategory(Long categoryId) {
        if (isResident()) {
            return getMyIssues();
        }

        return issueRepository.findByCategoryIdAndArchivedFalse(categoryId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUES,
            key = "T(com.smartresidential.backend.cache.IssueCacheKeys).byApartment(#apartmentId)",
            condition = "T(com.smartresidential.backend.multitenancy.TenantContext).getRoleName() != 'ROLE_RESIDENT'"
    )
    public List<IssueResponseDTO> getIssuesByApartment(Long apartmentId) {
        if (isResident()) {
            return getMyIssues();
        }

        return issueRepository.findByApartmentIdAndArchivedFalse(apartmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUES,
            key = "T(com.smartresidential.backend.cache.IssueCacheKeys).byCreatedBy(#userId)"
    )
    public List<IssueResponseDTO> getIssuesByCreatedBy(Long userId) {
        if (isResident()) {
            Long currentUserId = requireAuthenticatedUserId();
            if (!currentUserId.equals(userId)) {
                throw new ForbiddenException("Residents can only list their own issues.");
            }
        }

        return issueRepository.findByCreatedByIdAndArchivedFalse(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUES,
            key = "T(com.smartresidential.backend.cache.IssueCacheKeys).byTitle(#title)",
            condition = "T(com.smartresidential.backend.multitenancy.TenantContext).getRoleName() != 'ROLE_RESIDENT'"
    )
    public List<IssueResponseDTO> searchIssuesByTitle(String title) {
        if (isResident()) {
            return getMyIssues();
        }

        return issueRepository.findByTitleContainingIgnoreCaseAndArchivedFalse(title)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public IssueResponseDTO assignTechnician(Long issueId, Long technicianId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + technicianId));

        validateAssignableTechnician(technician);

        IssueAssignment assignment = new IssueAssignment();
        assignment.setIssue(issue);
        assignment.setTechnician(technician);
        issueAssignmentRepository.save(assignment);

        issue.setStatus(STATUS_ASSIGNED);
        Issue updatedIssue = issueRepository.save(issue);
        ensureMaintenanceWorkOrder(updatedIssue, resolveAssignmentRequester(technician));
        evictCurrentTenantIssueCache();
        scheduleAfterCommit(() -> notificationJob.notifyTechnicianAssigned(updatedIssue.getId(), technicianId));
        auditLogService.logCurrentUser("TECHNICIAN_ASSIGNED", "ISSUE", updatedIssue.getId());
        return mapToResponse(updatedIssue);
    }

    @Override
    public IssueResponseDTO changeStatus(Long issueId, String newStatus) {
        Long loggedInUserId = TenantContext.getUserId();

        if (loggedInUserId == null) {
            throw new UnauthorizedException("Authenticated user is required.");
        }

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        User changedBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + loggedInUserId));

        String oldStatus = issue.getStatus();
        updateIssueStatus(issue, newStatus);

        Issue updatedIssue = issueRepository.save(issue);

        IssueStatusHistory history = new IssueStatusHistory();
        history.setIssue(issue);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        issueStatusHistoryRepository.save(history);
        evictCurrentTenantIssueCache();
        scheduleAfterCommit(() -> notificationJob.notifyIssueStatusChanged(updatedIssue.getId(), newStatus));
        auditLogService.logCurrentUser("ISSUE_STATUS_CHANGED", "ISSUE", updatedIssue.getId());
        return mapToResponse(updatedIssue);
    }

    private void evictCurrentTenantIssueCache() {
        tenantCacheEvictor.evictCurrentTenant(CacheNames.ISSUES);
    }

    private Long requireAuthenticatedUserId() {
        Long currentUserId = TenantContext.getUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("Authenticated user is required.");
        }
        return currentUserId;
    }

    private boolean isResident() {
        return ROLE_RESIDENT.equals(TenantContext.getRoleName());
    }

    private Apartment resolveIssueApartment(CreateIssueRequest request, Long loggedInUserId) {
        if (isResident()) {
            ResidentProfile residentProfile = residentProfileRepository.findByUserId(loggedInUserId)
                    .orElseThrow(this::residentApartmentNotLinkedException);

            if (residentProfile.getApartment() == null) {
                throw residentApartmentNotLinkedException();
            }

            return residentProfile.getApartment();
        }

        return apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Apartment not found with id: " + request.getApartmentId()
                ));
    }

    private BadRequestException residentApartmentNotLinkedException() {
        return new BadRequestException("Your resident profile is not linked to an apartment yet.");
    }

    private void scheduleAutoClassification(Long issueId, Long requestedByUserId) {
        scheduleAfterCommit(() -> issueAutoClassificationJob.classifyAndAssign(issueId, requestedByUserId));
    }

    private void scheduleAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            return;
        }

        task.run();
    }

    private Issue autoAssignTechnician(Issue issue, User requestedBy) {
        IssueCategory category = issue.getCategory();
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
        scheduleAfterCommit(() -> notificationJob.notifyTechnicianAssigned(updatedIssue.getId(), technician.getId()));
        return updatedIssue;
    }

    private User resolveAssignmentRequester(User fallback) {
        Long currentUserId = TenantContext.getUserId();
        if (currentUserId == null) {
            return fallback;
        }

        return userRepository.findById(currentUserId).orElse(fallback);
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

    private String nullToBlank(String value) {
        return value == null ? "" : value;
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

    private IssueResponseDTO mapToResponse(Issue issue) {
        Optional<IssueAssignment> currentAssignment =
                issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(issue.getId())
                        .filter(assignment -> assignment.getTechnician() != null);
        return issueMapper.toResponse(issue, currentAssignment);
    }

    private void updateIssueStatus(Issue issue, String newStatus) {
        if (STATUS_ASSIGNED.equals(newStatus) && !issueAssignmentRepository.existsByIssueId(issue.getId())) {
            throw new IllegalArgumentException("Issue cannot be marked ASSIGNED without a technician.");
        }

        issue.setStatus(newStatus);
    }

    private void validateAssignableTechnician(User technician) {
        if (!Boolean.TRUE.equals(technician.getIsActive())) {
            throw new IllegalArgumentException("Technician user must be active.");
        }

        Role role = roleRepository.findById(technician.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Technician user role not found."));

        if (!ROLE_TECHNICIAN.equals(role.getName())) {
            throw new IllegalArgumentException("User must have ROLE_TECHNICIAN to be assigned.");
        }

        TechnicianProfile profile = technicianProfileRepository.findByUserId(technician.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Technician profile not found for user id: " + technician.getId()));

        if (!Boolean.TRUE.equals(profile.getIsAvailable())) {
            throw new IllegalArgumentException("Technician is not available for assignment.");
        }

        if (activeWorkload(profile, false) >= maxActiveIssues(profile)) {
            throw new IllegalArgumentException("Technician is at capacity.");
        }
    }

    private PageRequest buildPageRequest(IssueFilterRequest filter) {
        int page = filter != null && filter.getPage() != null && filter.getPage() >= 0
                ? filter.getPage()
                : DEFAULT_PAGE;

        int size = filter != null && filter.getSize() != null && filter.getSize() > 0
                ? Math.min(filter.getSize(), MAX_PAGE_SIZE)
                : DEFAULT_SIZE;

        String sortBy = filter != null && ALLOWED_SORT_FIELDS.contains(filter.getSortBy())
                ? filter.getSortBy()
                : "createdAt";

        Sort.Direction direction = filter != null && "asc".equalsIgnoreCase(filter.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
