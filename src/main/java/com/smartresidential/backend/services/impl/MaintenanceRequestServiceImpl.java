package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.maintenanceRequest.CreateMaintenanceRequestRequest;
import com.smartresidential.backend.dto.maintenanceRequest.MaintenanceRequestResponseDTO;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.IssueAssignment;
import com.smartresidential.backend.entities.MaintenanceRequest;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.ConflictException;
import com.smartresidential.backend.exceptions.ForbiddenException;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.IssueAssignmentRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.MaintenanceRequestRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.AuditLogService;
import com.smartresidential.backend.services.interfaces.MaintenanceRequestService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceRequestServiceImpl implements MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final IssueAssignmentRepository issueAssignmentRepository;
    private final AuditLogService auditLogService;

    public MaintenanceRequestServiceImpl(
            MaintenanceRequestRepository maintenanceRequestRepository,
            UserRepository userRepository,
            IssueRepository issueRepository,
            IssueAssignmentRepository issueAssignmentRepository,
            AuditLogService auditLogService
    ) {
        this.maintenanceRequestRepository = maintenanceRequestRepository;
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
        this.issueAssignmentRepository = issueAssignmentRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public MaintenanceRequestResponseDTO createMaintenanceRequest(CreateMaintenanceRequestRequest request) {
        Issue issue = issueRepository.findById(request.getIssueId())
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        User user = userRepository.findById(request.getRequestedById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (maintenanceRequestRepository.existsByIssue_Id(request.getIssueId())) {
            throw new ConflictException("Maintenance request already exists for this issue");
        }

        MaintenanceRequest maintenanceRequest = new MaintenanceRequest();
        maintenanceRequest.setIssue(issue);
        maintenanceRequest.setRequestedBy(user);
        maintenanceRequest.setDescription(request.getDescription());

        MaintenanceRequest savedRequest = maintenanceRequestRepository.save(maintenanceRequest);
        auditLogService.logCurrentUser("MAINTENANCE_REQUEST_CREATED", "MAINTENANCE_REQUEST", savedRequest.getId());

        return convertToResponseDTO(savedRequest);
    }

    @Override
    public MaintenanceRequestResponseDTO getMaintenanceRequestById(Long id) {
        MaintenanceRequest maintenanceRequest = maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance request not found"));

        return convertToResponseDTO(maintenanceRequest);
    }

    @Override
    public List<MaintenanceRequestResponseDTO> getAllMaintenanceRequests() {
        String roleName = TenantContext.getRoleName();
        Long userId = TenantContext.getUserId();

        if ("ROLE_RESIDENT".equals(roleName)) {
            throw new ForbiddenException("Residents cannot access maintenance work orders.");
        }

        return maintenanceRequestRepository.findAll()
                .stream()
                .filter(this::hasVisibleIssue)
                .filter(request -> !"ROLE_TECHNICIAN".equals(roleName)
                        || isAssignedToTechnician(request, userId))
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Override
    public boolean existsByIssueId(Long issueId) {
        return maintenanceRequestRepository.existsByIssue_Id(issueId);
    }

    private MaintenanceRequestResponseDTO convertToResponseDTO(MaintenanceRequest maintenanceRequest) {
        MaintenanceRequestResponseDTO dto = new MaintenanceRequestResponseDTO();
        dto.setId(maintenanceRequest.getId());
        dto.setIssueId(maintenanceRequest.getIssue() != null
                ? maintenanceRequest.getIssue().getId()
                : null);
        Issue issue = maintenanceRequest.getIssue();
        if (issue != null) {
            dto.setIssueTitle(issue.getTitle());
            dto.setIssueStatus(issue.getStatus());
            dto.setIssuePriority(issue.getPriority());
            dto.setWorkOrderStatus(issue.getStatus());
            issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(issue.getId())
                    .filter(assignment -> assignment.getTechnician() != null)
                    .ifPresent(assignment -> {
                        User technician = assignment.getTechnician();
                        dto.setAssignedTechnicianUserId(technician.getId());
                        dto.setAssignedTechnicianName(formatTechnicianName(technician));
                    });
        }
        dto.setRequestedById(maintenanceRequest.getRequestedBy() != null
                ? maintenanceRequest.getRequestedBy().getId()
                : null);
        dto.setDescription(maintenanceRequest.getDescription());
        dto.setRequestedAt(maintenanceRequest.getRequestedAt());
        return dto;
    }

    private boolean isAssignedToTechnician(MaintenanceRequest request, Long userId) {
        if (userId == null || request.getIssue() == null || request.getIssue().getId() == null) {
            return false;
        }

        return issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(request.getIssue().getId())
                .map(IssueAssignment::getTechnician)
                .map(User::getId)
                .filter(userId::equals)
                .isPresent();
    }

    private boolean hasVisibleIssue(MaintenanceRequest request) {
        Issue issue = request.getIssue();
        return issue != null && !Boolean.TRUE.equals(issue.getArchived());
    }

    private String formatTechnicianName(User technician) {
        if (technician == null) {
            return null;
        }

        String name = String.join(" ",
                technician.getFirstName() == null ? "" : technician.getFirstName(),
                technician.getLastName() == null ? "" : technician.getLastName()
        ).trim();

        return name.isBlank() ? technician.getEmail() : name;
    }
}
