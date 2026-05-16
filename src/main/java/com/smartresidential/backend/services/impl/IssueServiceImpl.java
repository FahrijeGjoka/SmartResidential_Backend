package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.issue.CreateIssueRequest;
import com.smartresidential.backend.dto.issue.IssueFilterRequest;
import com.smartresidential.backend.dto.issue.IssueResponseDTO;
import com.smartresidential.backend.dto.issue.UpdateIssueRequest;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.IssueAssignment;
import com.smartresidential.backend.entities.IssueCategory;
import com.smartresidential.backend.entities.IssueStatusHistory;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.cache.CacheNames;
import com.smartresidential.backend.cache.TenantCacheEvictor;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.exceptions.UnauthorizedException;
import com.smartresidential.backend.jobs.NotificationJob;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.ApartmentRepository;
import com.smartresidential.backend.repositories.IssueAssignmentRepository;
import com.smartresidential.backend.repositories.IssueCategoryRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.IssueStatusHistoryRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.IssueService;
import com.smartresidential.backend.specifications.IssueSpecification;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueServiceImpl implements IssueService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
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
    private final NotificationJob notificationJob;
    private final TenantCacheEvictor tenantCacheEvictor;

    @Override
    public IssueResponseDTO createIssue(CreateIssueRequest request) {
        Long loggedInUserId = TenantContext.getUserId();

        if (loggedInUserId == null) {
            throw new UnauthorizedException("Authenticated user is required.");
        }

        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Apartment not found with id: " + request.getApartmentId()
                ));

        User createdBy = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + loggedInUserId
                ));

        IssueCategory category = null;
        if (request.getCategoryId() != null) {
            category = issueCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Issue category not found with id: " + request.getCategoryId()
                    ));
        }

        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setStatus("OPEN");
        issue.setPriority(request.getPriority());
        issue.setApartment(apartment);
        issue.setCreatedBy(createdBy);
        issue.setCategory(category);

        Issue savedIssue = issueRepository.save(issue);
        evictCurrentTenantIssueCache();
        notificationJob.notifyIssueCreated(savedIssue.getId());
        return mapToResponse(savedIssue);
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
            issue.setStatus(request.getStatus());
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
        }

        Issue updatedIssue = issueRepository.save(issue);
        evictCurrentTenantIssueCache();
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
    @Cacheable(
            cacheNames = CacheNames.ISSUES,
            key = "T(com.smartresidential.backend.cache.IssueCacheKeys).all()"
    )
    public List<IssueResponseDTO> getAllIssues() {
        return issueRepository.findAll()
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

        issueRepository.delete(issue);
        evictCurrentTenantIssueCache();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUES,
            key = "T(com.smartresidential.backend.cache.IssueCacheKeys).byStatus(#status)"
    )
    public List<IssueResponseDTO> getIssuesByStatus(String status) {
        return issueRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUES,
            key = "T(com.smartresidential.backend.cache.IssueCacheKeys).byPriority(#priority)"
    )
    public List<IssueResponseDTO> getIssuesByPriority(String priority) {
        return issueRepository.findByPriority(priority)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUES,
            key = "T(com.smartresidential.backend.cache.IssueCacheKeys).byCategory(#categoryId)"
    )
    public List<IssueResponseDTO> getIssuesByCategory(Long categoryId) {
        return issueRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUES,
            key = "T(com.smartresidential.backend.cache.IssueCacheKeys).byApartment(#apartmentId)"
    )
    public List<IssueResponseDTO> getIssuesByApartment(Long apartmentId) {
        return issueRepository.findByApartmentId(apartmentId)
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
        return issueRepository.findByCreatedById(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUES,
            key = "T(com.smartresidential.backend.cache.IssueCacheKeys).byTitle(#title)"
    )
    public List<IssueResponseDTO> searchIssuesByTitle(String title) {
        return issueRepository.findByTitleContainingIgnoreCase(title)
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

        IssueAssignment assignment = new IssueAssignment();
        assignment.setIssue(issue);
        assignment.setTechnician(technician);
        issueAssignmentRepository.save(assignment);

        issue.setStatus("ASSIGNED");
        Issue updatedIssue = issueRepository.save(issue);
        evictCurrentTenantIssueCache();
        notificationJob.notifyTechnicianAssigned(updatedIssue.getId(), technicianId);
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
        issue.setStatus(newStatus);

        Issue updatedIssue = issueRepository.save(issue);

        IssueStatusHistory history = new IssueStatusHistory();
        history.setIssue(issue);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        issueStatusHistoryRepository.save(history);
        evictCurrentTenantIssueCache();
        notificationJob.notifyIssueStatusChanged(updatedIssue.getId(), newStatus);
        return mapToResponse(updatedIssue);
    }

    private void evictCurrentTenantIssueCache() {
        tenantCacheEvictor.evictCurrentTenant(CacheNames.ISSUES);
    }

    private IssueResponseDTO mapToResponse(Issue issue) {
        IssueResponseDTO response = new IssueResponseDTO();
        response.setId(issue.getId());
        response.setTitle(issue.getTitle());
        response.setDescription(issue.getDescription());
        response.setStatus(issue.getStatus());
        response.setPriority(issue.getPriority());
        response.setApartmentId(issue.getApartment().getId());
        response.setCreatedById(issue.getCreatedBy().getId());
        response.setCategoryId(issue.getCategory() != null ? issue.getCategory().getId() : null);
        response.setCreatedAt(issue.getCreatedAt());
        response.setUpdatedAt(issue.getUpdatedAt());
        return response;
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
