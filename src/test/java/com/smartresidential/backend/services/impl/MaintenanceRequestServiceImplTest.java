package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.maintenanceRequest.MaintenanceRequestResponseDTO;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.IssueAssignment;
import com.smartresidential.backend.entities.MaintenanceRequest;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.ConflictException;
import com.smartresidential.backend.exceptions.ForbiddenException;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.IssueAssignmentRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.MaintenanceRequestRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.dto.maintenanceRequest.CreateMaintenanceRequestRequest;
import com.smartresidential.backend.services.interfaces.AuditLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintenanceRequestServiceImplTest {

    @Mock
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private IssueAssignmentRepository issueAssignmentRepository;

    @Mock
    private AuditLogService auditLogService;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getAllMaintenanceRequestsReturnsEmptyListWhenNoneExist() {
        MaintenanceRequestServiceImpl service =
                new MaintenanceRequestServiceImpl(
                        maintenanceRequestRepository,
                        userRepository,
                        issueRepository,
                        issueAssignmentRepository,
                        auditLogService
                );

        when(maintenanceRequestRepository.findAll()).thenReturn(List.of());

        List<MaintenanceRequestResponseDTO> response = service.getAllMaintenanceRequests();

        assertThat(response).isEmpty();
    }

    @Test
    void getAllMaintenanceRequestsDoesNotReturnOrphanedMaintenanceRows() {
        MaintenanceRequestServiceImpl service =
                new MaintenanceRequestServiceImpl(
                        maintenanceRequestRepository,
                        userRepository,
                        issueRepository,
                        issueAssignmentRepository,
                        auditLogService
                );
        MaintenanceRequest maintenanceRequest = new MaintenanceRequest();
        maintenanceRequest.setId(10L);
        maintenanceRequest.setDescription("Escalate manually");

        when(maintenanceRequestRepository.findAll()).thenReturn(List.of(maintenanceRequest));

        List<MaintenanceRequestResponseDTO> response = service.getAllMaintenanceRequests();

        assertThat(response).isEmpty();
    }

    @Test
    void getAllMaintenanceRequestsDoesNotReturnArchivedIssues() {
        MaintenanceRequestServiceImpl service = service();
        Issue archivedIssue = issue(10L, "Archived issue", "ASSIGNED", "HIGH");
        archivedIssue.setArchived(true);
        MaintenanceRequest maintenanceRequest = maintenanceRequest(1L, archivedIssue, user(1L, "Admin", "User"));

        when(maintenanceRequestRepository.findAll()).thenReturn(List.of(maintenanceRequest));

        List<MaintenanceRequestResponseDTO> response = service.getAllMaintenanceRequests();

        assertThat(response).isEmpty();
    }

    @Test
    void technicianSeesOnlyAssignedWorkOrders() {
        MaintenanceRequestServiceImpl service = service();
        User technician = user(20L, "Ada", "Tech");
        Issue assignedIssue = issue(10L, "Assigned issue", "ASSIGNED", "HIGH");
        Issue otherIssue = issue(11L, "Other issue", "ASSIGNED", "LOW");
        MaintenanceRequest assigned = maintenanceRequest(1L, assignedIssue, user(1L, "Admin", "User"));
        MaintenanceRequest other = maintenanceRequest(2L, otherIssue, user(1L, "Admin", "User"));
        TenantContext.set(1L, "tenant", "tenant", technician.getId(), "ROLE_TECHNICIAN");

        when(maintenanceRequestRepository.findAll()).thenReturn(List.of(assigned, other));
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(assignedIssue.getId()))
                .thenReturn(Optional.of(assignment(100L, assignedIssue, technician)));
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(otherIssue.getId()))
                .thenReturn(Optional.of(assignment(101L, otherIssue, user(21L, "Other", "Tech"))));

        List<MaintenanceRequestResponseDTO> response = service.getAllMaintenanceRequests();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getIssueId()).isEqualTo(assignedIssue.getId());
        assertThat(response.get(0).getAssignedTechnicianUserId()).isEqualTo(technician.getId());
        assertThat(response.get(0).getAssignedTechnicianName()).isEqualTo("Ada Tech");
        assertThat(response.get(0).getWorkOrderStatus()).isEqualTo("ASSIGNED");
    }

    @Test
    void residentCannotListMaintenanceWorkOrders() {
        MaintenanceRequestServiceImpl service = service();
        TenantContext.set(1L, "tenant", "tenant", 30L, "ROLE_RESIDENT");

        assertThatThrownBy(service::getAllMaintenanceRequests)
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Residents cannot access maintenance work orders.");
    }

    @Test
    void creatingDuplicateMaintenanceRequestFailsClearly() {
        MaintenanceRequestServiceImpl service = service();
        CreateMaintenanceRequestRequest request = new CreateMaintenanceRequestRequest();
        request.setIssueId(10L);
        request.setRequestedById(1L);
        request.setDescription("Duplicate");

        when(issueRepository.findById(request.getIssueId()))
                .thenReturn(Optional.of(issue(10L, "Issue", "ASSIGNED", "MEDIUM")));
        when(userRepository.findById(request.getRequestedById()))
                .thenReturn(Optional.of(user(1L, "Admin", "User")));
        when(maintenanceRequestRepository.existsByIssue_Id(request.getIssueId())).thenReturn(true);

        assertThatThrownBy(() -> service.createMaintenanceRequest(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Maintenance request already exists for this issue");
    }

    private MaintenanceRequestServiceImpl service() {
        return new MaintenanceRequestServiceImpl(
                maintenanceRequestRepository,
                userRepository,
                issueRepository,
                issueAssignmentRepository,
                auditLogService
        );
    }

    private MaintenanceRequest maintenanceRequest(Long id, Issue issue, User requestedBy) {
        MaintenanceRequest request = new MaintenanceRequest();
        request.setId(id);
        request.setIssue(issue);
        request.setRequestedBy(requestedBy);
        request.setDescription("Work order");
        return request;
    }

    private Issue issue(Long id, String title, String status, String priority) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setTitle(title);
        issue.setStatus(status);
        issue.setPriority(priority);
        return issue;
    }

    private User user(Long id, String firstName, String lastName) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail("user" + id + "@example.com");
        return user;
    }

    private IssueAssignment assignment(Long id, Issue issue, User technician) {
        IssueAssignment assignment = new IssueAssignment();
        assignment.setId(id);
        assignment.setIssue(issue);
        assignment.setTechnician(technician);
        return assignment;
    }
}
