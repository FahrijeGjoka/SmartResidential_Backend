package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.ai.IssueCategoryMatch;
import com.smartresidential.backend.dto.issue.CreateIssueRequest;
import com.smartresidential.backend.dto.issue.IssueResponseDTO;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.Building;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.IssueAssignment;
import com.smartresidential.backend.entities.IssueCategory;
import com.smartresidential.backend.entities.MaintenanceRequest;
import com.smartresidential.backend.entities.ResidentProfile;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.TechnicianProfile;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.cache.TenantCacheEvictor;
import com.smartresidential.backend.exceptions.BadRequestException;
import com.smartresidential.backend.exceptions.ForbiddenException;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.jobs.NotificationJob;
import com.smartresidential.backend.mapper.IssueMapper;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.ApartmentRepository;
import com.smartresidential.backend.repositories.IssueAssignmentRepository;
import com.smartresidential.backend.repositories.IssueCategoryRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.IssueStatusHistoryRepository;
import com.smartresidential.backend.repositories.MaintenanceRequestRepository;
import com.smartresidential.backend.repositories.ResidentProfileRepository;
import com.smartresidential.backend.repositories.RoleRepository;
import com.smartresidential.backend.repositories.TechnicianProfileRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.IssueCategoryMatcherService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueServiceImplTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private IssueCategoryRepository issueCategoryRepository;

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IssueAssignmentRepository issueAssignmentRepository;

    @Mock
    private IssueStatusHistoryRepository issueStatusHistoryRepository;

    @Mock
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @Mock
    private ResidentProfileRepository residentProfileRepository;

    @Mock
    private TechnicianProfileRepository technicianProfileRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private IssueCategoryMatcherService issueCategoryMatcherService;

    @Mock
    private NotificationJob notificationJob;

    @Mock
    private TenantCacheEvictor tenantCacheEvictor;

    private IssueServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new IssueServiceImpl(
                issueRepository,
                issueCategoryRepository,
                apartmentRepository,
                userRepository,
                issueAssignmentRepository,
                issueStatusHistoryRepository,
                maintenanceRequestRepository,
                residentProfileRepository,
                technicianProfileRepository,
                roleRepository,
                issueCategoryMatcherService,
                notificationJob,
                new IssueMapper(),
                tenantCacheEvictor
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void manualStatusChangeToAssignedWithoutTechnicianFails() {
        Issue issue = issue(10L, "OPEN");
        User actor = user(1L, 1L, true);
        TenantContext.set(1L, "tenant", "tenant", actor.getId(), "ROLE_ADMIN");

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(userRepository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(issueAssignmentRepository.existsByIssueId(issue.getId())).thenReturn(false);

        assertThatThrownBy(() -> service.changeStatus(issue.getId(), "ASSIGNED"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Issue cannot be marked ASSIGNED without a technician.");

        verify(issueRepository, never()).save(any());
        assertThat(issue.getStatus()).isEqualTo("OPEN");
    }

    @Test
    void assigningTechnicianCreatesAssignmentAndSetsStatusAssigned() {
        Issue issue = issue(10L, "OPEN");
        User admin = user(1L, 1L, true);
        User technician = user(20L, 3L, true);
        Role technicianRole = role(3L, "ROLE_TECHNICIAN");
        TechnicianProfile profile = technicianProfile(30L, technician);
        IssueAssignment assignment = assignment(40L, issue, technician);
        TenantContext.set(1L, "tenant", "tenant", admin.getId(), "ROLE_ADMIN");

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(userRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(roleRepository.findById(technician.getRoleId())).thenReturn(Optional.of(technicianRole));
        when(technicianProfileRepository.findByUserId(technician.getId())).thenReturn(Optional.of(profile));
        when(issueAssignmentRepository.save(any(IssueAssignment.class))).thenReturn(assignment);
        when(issueRepository.save(issue)).thenReturn(issue);
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(issue.getId()))
                .thenReturn(Optional.of(assignment));

        IssueResponseDTO response = service.assignTechnician(issue.getId(), technician.getId());

        ArgumentCaptor<IssueAssignment> assignmentCaptor = ArgumentCaptor.forClass(IssueAssignment.class);
        verify(issueAssignmentRepository).save(assignmentCaptor.capture());
        assertThat(assignmentCaptor.getValue().getIssue()).isSameAs(issue);
        assertThat(assignmentCaptor.getValue().getTechnician()).isSameAs(technician);
        assertThat(issue.getStatus()).isEqualTo("ASSIGNED");
        assertThat(response.getStatus()).isEqualTo("ASSIGNED");
        assertThat(response.getAssignedTechnicianId()).isEqualTo(technician.getId());
        assertThat(response.getAssignedTechnicianUserId()).isEqualTo(technician.getId());
        ArgumentCaptor<MaintenanceRequest> maintenanceCaptor = ArgumentCaptor.forClass(MaintenanceRequest.class);
        verify(maintenanceRequestRepository).save(maintenanceCaptor.capture());
        assertThat(maintenanceCaptor.getValue().getIssue()).isSameAs(issue);
        assertThat(maintenanceCaptor.getValue().getRequestedBy()).isSameAs(admin);
        verify(notificationJob).notifyTechnicianAssigned(issue.getId(), technician.getId());
    }

    @Test
    void repeatedAssignmentDoesNotCreateDuplicateMaintenanceWorkOrder() {
        Issue issue = issue(10L, "OPEN");
        User technician = user(20L, 3L, true);
        Role technicianRole = role(3L, "ROLE_TECHNICIAN");
        TechnicianProfile profile = technicianProfile(30L, technician);
        IssueAssignment assignment = assignment(40L, issue, technician);

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(userRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(roleRepository.findById(technician.getRoleId())).thenReturn(Optional.of(technicianRole));
        when(technicianProfileRepository.findByUserId(technician.getId())).thenReturn(Optional.of(profile));
        when(issueAssignmentRepository.save(any(IssueAssignment.class))).thenReturn(assignment);
        when(issueRepository.save(issue)).thenReturn(issue);
        when(maintenanceRequestRepository.existsByIssue_Id(issue.getId())).thenReturn(true);
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(issue.getId()))
                .thenReturn(Optional.of(assignment));

        service.assignTechnician(issue.getId(), technician.getId());

        verify(maintenanceRequestRepository, never()).save(any());
    }

    @Test
    void assigningMissingUserFailsAsNotFound() {
        Issue issue = issue(10L, "OPEN");

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(userRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignTechnician(issue.getId(), 20L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 20");

        verify(issueAssignmentRepository, never()).save(any());
        verify(issueRepository, never()).save(any());
    }

    @Test
    void assigningNonTechnicianUserFailsAsBadRequest() {
        Issue issue = issue(10L, "OPEN");
        User resident = user(20L, 4L, true);
        Role residentRole = role(4L, "ROLE_RESIDENT");

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));
        when(roleRepository.findById(resident.getRoleId())).thenReturn(Optional.of(residentRole));

        assertThatThrownBy(() -> service.assignTechnician(issue.getId(), resident.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User must have ROLE_TECHNICIAN to be assigned.");

        verify(issueAssignmentRepository, never()).save(any());
        verify(issueRepository, never()).save(any());
    }

    @Test
    void assigningInactiveTechnicianFailsAsBadRequest() {
        Issue issue = issue(10L, "OPEN");
        User technician = user(20L, 3L, false);

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(userRepository.findById(technician.getId())).thenReturn(Optional.of(technician));

        assertThatThrownBy(() -> service.assignTechnician(issue.getId(), technician.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Technician user must be active.");

        verify(issueAssignmentRepository, never()).save(any());
        verify(issueRepository, never()).save(any());
    }

    @Test
    void assigningTechnicianWithoutProfileFailsAsBadRequest() {
        Issue issue = issue(10L, "OPEN");
        User technician = user(20L, 3L, true);
        Role technicianRole = role(3L, "ROLE_TECHNICIAN");

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(userRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(roleRepository.findById(technician.getRoleId())).thenReturn(Optional.of(technicianRole));
        when(technicianProfileRepository.findByUserId(technician.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignTechnician(issue.getId(), technician.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Technician profile not found for user id: 20");

        verify(issueAssignmentRepository, never()).save(any());
        verify(issueRepository, never()).save(any());
    }

    @Test
    void assignedIssueCanMoveToProgressResolvedAndClosed() {
        User actor = user(1L, 1L, true);
        TenantContext.set(1L, "tenant", "tenant", actor.getId(), "ROLE_ADMIN");
        Issue issue = issue(10L, "ASSIGNED");
        User technician = user(20L, 3L, true);
        IssueAssignment assignment = assignment(40L, issue, technician);

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(userRepository.findById(actor.getId())).thenReturn(Optional.of(actor));
        when(issueRepository.save(issue)).thenReturn(issue);
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(issue.getId()))
                .thenReturn(Optional.of(assignment));

        assertThat(service.changeStatus(issue.getId(), "IN_PROGRESS").getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(service.changeStatus(issue.getId(), "RESOLVED").getStatus()).isEqualTo("RESOLVED");
        assertThat(service.changeStatus(issue.getId(), "CLOSED").getStatus()).isEqualTo("CLOSED");
        assertThat(service.getIssueById(issue.getId()).getAssignedTechnicianId()).isEqualTo(technician.getId());
    }

    @Test
    void assignedIssueWithoutAssignmentIsNotReturnedAsNullTechnician() {
        Issue issue = issue(10L, "ASSIGNED");

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(issue.getId()))
                .thenReturn(Optional.empty());

        IssueResponseDTO response = service.getIssueById(issue.getId());

        assertThat(response.getStatus()).isEqualTo("OPEN");
        assertThat(response.getAssignedTechnicianUserId()).isNull();
    }

    @Test
    void residentCreatesIssueWithLinkedApartment() {
        User resident = user(10L, 4L, true);
        Apartment linkedApartment = apartment(20L);
        CreateIssueRequest request = createIssueRequest(999L);
        TenantContext.set(1L, "tenant", "tenant", resident.getId(), "ROLE_RESIDENT");

        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));
        when(residentProfileRepository.findByUserId(resident.getId()))
                .thenReturn(Optional.of(residentProfile(30L, resident, linkedApartment)));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(40L);
            return issue;
        });
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(40L))
                .thenReturn(Optional.empty());

        IssueResponseDTO response = service.createIssue(request);

        ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
        verify(issueRepository).save(issueCaptor.capture());
        assertThat(issueCaptor.getValue().getApartment()).isSameAs(linkedApartment);
        assertThat(response.getApartmentId()).isEqualTo(linkedApartment.getId());
        verify(apartmentRepository, never()).findById(999L);
    }

    @Test
    void residentCannotCreateIssueForAnotherApartmentBySendingApartmentId() {
        User resident = user(10L, 4L, true);
        Apartment linkedApartment = apartment(20L);
        CreateIssueRequest request = createIssueRequest(999L);
        TenantContext.set(1L, "tenant", "tenant", resident.getId(), "ROLE_RESIDENT");

        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));
        when(residentProfileRepository.findByUserId(resident.getId()))
                .thenReturn(Optional.of(residentProfile(30L, resident, linkedApartment)));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(40L);
            return issue;
        });
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(40L))
                .thenReturn(Optional.empty());

        IssueResponseDTO response = service.createIssue(request);

        assertThat(response.getApartmentId()).isEqualTo(linkedApartment.getId());
        verify(apartmentRepository, never()).findById(999L);
    }

    @Test
    void residentWithoutLinkedApartmentGetsBadRequest() {
        User resident = user(10L, 4L, true);
        CreateIssueRequest request = createIssueRequest(999L);
        TenantContext.set(1L, "tenant", "tenant", resident.getId(), "ROLE_RESIDENT");

        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));
        when(residentProfileRepository.findByUserId(resident.getId()))
                .thenReturn(Optional.of(residentProfile(30L, resident, null)));

        assertThatThrownBy(() -> service.createIssue(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Your resident profile is not linked to an apartment yet.");

        verify(issueRepository, never()).save(any());
    }

    @Test
    void residentWithoutProfileGetsBadRequestWhenCreatingIssue() {
        User resident = user(10L, 4L, true);
        CreateIssueRequest request = createIssueRequest(999L);
        TenantContext.set(1L, "tenant", "tenant", resident.getId(), "ROLE_RESIDENT");

        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));
        when(residentProfileRepository.findByUserId(resident.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createIssue(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Your resident profile is not linked to an apartment yet.");

        verify(issueRepository, never()).save(any());
    }

    @Test
    void staffCreatesIssueWithExplicitApartmentId() {
        User staff = user(10L, 2L, true);
        Apartment requestedApartment = apartment(999L);
        CreateIssueRequest request = createIssueRequest(requestedApartment.getId());
        TenantContext.set(1L, "tenant", "tenant", staff.getId(), "ROLE_STAFF");

        when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
        when(apartmentRepository.findById(requestedApartment.getId())).thenReturn(Optional.of(requestedApartment));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(40L);
            return issue;
        });
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(40L))
                .thenReturn(Optional.empty());

        IssueResponseDTO response = service.createIssue(request);

        assertThat(response.getApartmentId()).isEqualTo(requestedApartment.getId());
        verify(apartmentRepository).findById(requestedApartment.getId());
        verify(residentProfileRepository, never()).findByUserId(staff.getId());
    }

    @Test
    void residentCreateAutoCategorizesAndAssignsMatchingAvailableSpecialist() {
        User resident = user(10L, 4L, true);
        User technician = user(20L, 3L, true);
        Apartment linkedApartment = apartment(30L);
        IssueCategory plumbing = issueCategory(40L, "Plumbing", "Plumbing");
        IssueAssignment assignment = assignment(60L, null, technician);
        CreateIssueRequest request = createIssueRequest(999L);
        request.setCategoryId(777L);
        TenantContext.set(1L, "tenant", "tenant", resident.getId(), "ROLE_RESIDENT");

        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));
        when(residentProfileRepository.findByUserId(resident.getId()))
                .thenReturn(Optional.of(residentProfile(31L, resident, linkedApartment)));
        when(issueCategoryMatcherService.matchCategory(request.getTitle(), request.getDescription()))
                .thenReturn(Optional.of(new IssueCategoryMatch(
                        plumbing,
                        0.91,
                        "The description mentions leaking water."
                )));
        when(technicianProfileRepository.findByIsAvailableTrue())
                .thenReturn(List.of(technicianProfile(50L, technician, "Plumbing")));
        when(roleRepository.findById(technician.getRoleId())).thenReturn(Optional.of(role(3L, "ROLE_TECHNICIAN")));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(70L);
            assignment.setIssue(issue);
            return issue;
        });
        when(issueAssignmentRepository.save(any(IssueAssignment.class))).thenReturn(assignment);
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(70L))
                .thenReturn(Optional.of(assignment));

        IssueResponseDTO response = service.createIssue(request);

        assertThat(response.getStatus()).isEqualTo("ASSIGNED");
        assertThat(response.getApartmentId()).isEqualTo(linkedApartment.getId());
        assertThat(response.getCategoryId()).isEqualTo(plumbing.getId());
        assertThat(response.getCategoryName()).isEqualTo("Plumbing");
        assertThat(response.getAssignedTechnicianUserId()).isEqualTo(technician.getId());
        assertThat(response.getAiCategoryConfidence()).isEqualTo(0.91);
        assertThat(response.getAiCategoryReason()).contains("leaking water");
        ArgumentCaptor<MaintenanceRequest> maintenanceCaptor = ArgumentCaptor.forClass(MaintenanceRequest.class);
        verify(maintenanceRequestRepository).save(maintenanceCaptor.capture());
        assertThat(maintenanceCaptor.getValue().getIssue().getId()).isEqualTo(70L);
        assertThat(maintenanceCaptor.getValue().getRequestedBy()).isSameAs(resident);
        verify(issueCategoryRepository, never()).findById(777L);
    }

    @Test
    void autoAssignmentFallsBackToGeneralMaintenanceWhenNoMatchingSpecialistAvailable() {
        User staff = user(10L, 2L, true);
        User generalTechnician = user(20L, 3L, true);
        Apartment requestedApartment = apartment(30L);
        IssueCategory plumbing = issueCategory(40L, "Plumbing", "Plumbing");
        IssueAssignment assignment = assignment(60L, null, generalTechnician);
        CreateIssueRequest request = createIssueRequest(requestedApartment.getId());
        TenantContext.set(1L, "tenant", "tenant", staff.getId(), "ROLE_STAFF");

        when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
        when(apartmentRepository.findById(requestedApartment.getId())).thenReturn(Optional.of(requestedApartment));
        when(issueCategoryMatcherService.matchCategory(request.getTitle(), request.getDescription()))
                .thenReturn(Optional.of(new IssueCategoryMatch(plumbing, 0.9, "Matched Plumbing.")));
        when(technicianProfileRepository.findByIsAvailableTrue())
                .thenReturn(List.of(technicianProfile(50L, generalTechnician, "General maintenance")));
        when(roleRepository.findById(generalTechnician.getRoleId())).thenReturn(Optional.of(role(3L, "ROLE_TECHNICIAN")));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(70L);
            assignment.setIssue(issue);
            return issue;
        });
        when(issueAssignmentRepository.save(any(IssueAssignment.class))).thenReturn(assignment);
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(70L))
                .thenReturn(Optional.of(assignment));

        IssueResponseDTO response = service.createIssue(request);

        assertThat(response.getStatus()).isEqualTo("ASSIGNED");
        assertThat(response.getAssignedTechnicianUserId()).isEqualTo(generalTechnician.getId());
    }

    @Test
    void noAvailableTechnicianLeavesIssueOpenAndUnassigned() {
        User staff = user(10L, 2L, true);
        Apartment requestedApartment = apartment(30L);
        IssueCategory plumbing = issueCategory(40L, "Plumbing", "Plumbing");
        CreateIssueRequest request = createIssueRequest(requestedApartment.getId());
        TenantContext.set(1L, "tenant", "tenant", staff.getId(), "ROLE_STAFF");

        when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
        when(apartmentRepository.findById(requestedApartment.getId())).thenReturn(Optional.of(requestedApartment));
        when(issueCategoryMatcherService.matchCategory(request.getTitle(), request.getDescription()))
                .thenReturn(Optional.of(new IssueCategoryMatch(plumbing, 0.9, "Matched Plumbing.")));
        when(technicianProfileRepository.findByIsAvailableTrue()).thenReturn(List.of());
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(70L);
            return issue;
        });
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(70L))
                .thenReturn(Optional.empty());

        IssueResponseDTO response = service.createIssue(request);

        assertThat(response.getStatus()).isEqualTo("OPEN");
        assertThat(response.getAssignedTechnicianUserId()).isNull();
        verify(issueAssignmentRepository, never()).save(any());
        verify(maintenanceRequestRepository, never()).save(any());
    }

    @Test
    void technicianAtCapacityLeavesIssueOpenAndDoesNotChangeManualAvailability() {
        User staff = user(10L, 2L, true);
        User technician = user(20L, 3L, true);
        Apartment requestedApartment = apartment(30L);
        IssueCategory plumbing = issueCategory(40L, "Plumbing", "Plumbing");
        TechnicianProfile technicianProfile = technicianProfile(50L, technician, "Plumbing");
        technicianProfile.setMaxActiveIssues(1);
        CreateIssueRequest request = createIssueRequest(requestedApartment.getId());
        TenantContext.set(1L, "tenant", "tenant", staff.getId(), "ROLE_STAFF");

        when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
        when(apartmentRepository.findById(requestedApartment.getId())).thenReturn(Optional.of(requestedApartment));
        when(issueCategoryMatcherService.matchCategory(request.getTitle(), request.getDescription()))
                .thenReturn(Optional.of(new IssueCategoryMatch(plumbing, 0.9, "Matched Plumbing.")));
        when(technicianProfileRepository.findByIsAvailableTrue()).thenReturn(List.of(technicianProfile));
        when(roleRepository.findById(technician.getRoleId())).thenReturn(Optional.of(role(3L, "ROLE_TECHNICIAN")));
        when(issueAssignmentRepository.findByTechnicianId(technician.getId()))
                .thenReturn(List.of(assignment(80L, workloadIssue(81L, "ASSIGNED", "MEDIUM"), technician)));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(70L);
            return issue;
        });
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(70L))
                .thenReturn(Optional.empty());

        IssueResponseDTO response = service.createIssue(request);

        assertThat(response.getStatus()).isEqualTo("OPEN");
        assertThat(response.getAssignedTechnicianUserId()).isNull();
        assertThat(technicianProfile.getIsAvailable()).isTrue();
        verify(issueAssignmentRepository, never()).save(any());
    }

    @Test
    void urgentIssueChoosesTechnicianWithLowerActiveHighPriorityWorkload() {
        User selected = user(20L, 3L, true);
        User busy = user(21L, 3L, true);

        IssueResponseDTO response = createIssueWithCandidates(
                "URGENT",
                List.of(technicianProfile(50L, selected, "Plumbing"), technicianProfile(51L, busy, "Plumbing")),
                selected,
                List.of(),
                List.of(assignment(80L, workloadIssue(81L, "ASSIGNED", "HIGH"), busy))
        );

        assertThat(response.getAssignedTechnicianUserId()).isEqualTo(selected.getId());
    }

    @Test
    void mediumIssueChoosesTechnicianWithLowerTotalActiveWorkload() {
        User selected = user(20L, 3L, true);
        User busy = user(21L, 3L, true);

        IssueResponseDTO response = createIssueWithCandidates(
                "MEDIUM",
                List.of(technicianProfile(50L, selected, "Plumbing"), technicianProfile(51L, busy, "Plumbing")),
                selected,
                List.of(),
                List.of(assignment(80L, workloadIssue(81L, "IN_PROGRESS", "LOW"), busy))
        );

        assertThat(response.getAssignedTechnicianUserId()).isEqualTo(selected.getId());
    }

    @Test
    void workloadTieBreakUsesLeastRecentlyAssignedTechnician() {
        User leastRecent = user(20L, 3L, true);
        User recent = user(21L, 3L, true);
        IssueAssignment oldAssignment = assignment(80L, workloadIssue(81L, "CLOSED", "LOW"), leastRecent);
        oldAssignment.setAssignedAt(LocalDateTime.of(2026, 5, 1, 8, 0));
        IssueAssignment recentAssignment = assignment(82L, workloadIssue(83L, "CLOSED", "LOW"), recent);
        recentAssignment.setAssignedAt(LocalDateTime.of(2026, 5, 16, 8, 0));

        IssueResponseDTO response = createIssueWithCandidates(
                "LOW",
                List.of(technicianProfile(50L, leastRecent, "Plumbing"), technicianProfile(51L, recent, "Plumbing")),
                leastRecent,
                List.of(oldAssignment),
                List.of(recentAssignment)
        );

        assertThat(response.getAssignedTechnicianUserId()).isEqualTo(leastRecent.getId());
    }

    @Test
    void residentCanGetMyIssues() {
        User resident = user(10L, 4L, true);
        Issue ownIssue = issue(100L, "OPEN", resident);
        TenantContext.set(1L, "tenant", "tenant", resident.getId(), "ROLE_RESIDENT");

        when(issueRepository.findByCreatedById(resident.getId())).thenReturn(List.of(ownIssue));
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(ownIssue.getId()))
                .thenReturn(Optional.empty());

        List<IssueResponseDTO> response = service.getMyIssues();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCreatedById()).isEqualTo(resident.getId());
    }

    @Test
    void residentMineListingKeepsCurrentTenantContext() {
        User resident = user(10L, 4L, true);
        Issue ownIssue = issue(100L, "OPEN", resident);
        TenantContext.set(7L, "tenant_alpha", "alpha", resident.getId(), "ROLE_RESIDENT");

        when(issueRepository.findByCreatedById(resident.getId())).thenReturn(List.of(ownIssue));
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(ownIssue.getId()))
                .thenReturn(Optional.empty());

        List<IssueResponseDTO> response = service.getMyIssues();

        assertThat(response).hasSize(1);
        assertThat(TenantContext.getTenantId()).isEqualTo(7L);
        assertThat(TenantContext.getSchemaName()).isEqualTo("tenant_alpha");
        assertThat(TenantContext.getUserId()).isEqualTo(resident.getId());
        verify(issueRepository).findByCreatedById(resident.getId());
    }

    @Test
    void residentCannotListAnotherResidentsIssues() {
        User resident = user(10L, 4L, true);
        TenantContext.set(1L, "tenant", "tenant", resident.getId(), "ROLE_RESIDENT");

        assertThatThrownBy(() -> service.getIssuesByCreatedBy(11L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Residents can only list their own issues.");

        verify(issueRepository, never()).findByCreatedById(11L);
    }

    @Test
    void residentBroadIssueListReturnsOnlyOwnIssues() {
        User resident = user(10L, 4L, true);
        Issue ownIssue = issue(100L, "OPEN", resident);
        TenantContext.set(1L, "tenant", "tenant", resident.getId(), "ROLE_RESIDENT");

        when(issueRepository.findByCreatedById(resident.getId())).thenReturn(List.of(ownIssue));
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(ownIssue.getId()))
                .thenReturn(Optional.empty());

        List<IssueResponseDTO> response = service.getAllIssues();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCreatedById()).isEqualTo(resident.getId());
        verify(issueRepository, never()).findAll();
    }

    @Test
    void staffCanListAllIssues() {
        User resident = user(10L, 4L, true);
        Issue issue = issue(100L, "OPEN", resident);
        TenantContext.set(1L, "tenant", "tenant", 20L, "ROLE_STAFF");

        when(issueRepository.findAll()).thenReturn(List.of(issue));
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(issue.getId()))
                .thenReturn(Optional.empty());

        List<IssueResponseDTO> response = service.getAllIssues();

        assertThat(response).hasSize(1);
        verify(issueRepository).findAll();
    }

    @Test
    void adminIssueListReturnsEmptyListWhenNoIssuesExist() {
        TenantContext.set(1L, "tenant", "tenant", 30L, "ROLE_ADMIN");

        when(issueRepository.findAll()).thenReturn(List.of());

        List<IssueResponseDTO> response = service.getAllIssues();

        assertThat(response).isEmpty();
        verify(issueRepository).findAll();
    }

    @Test
    void adminIssueListDoesNotReturnAssignedWithoutTechnicianWhenAssignmentMissing() {
        User resident = user(10L, 4L, true);
        Issue issue = issue(100L, "ASSIGNED", resident);
        TenantContext.set(1L, "tenant", "tenant", 30L, "ROLE_ADMIN");

        when(issueRepository.findAll()).thenReturn(List.of(issue));
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(issue.getId()))
                .thenReturn(Optional.empty());

        List<IssueResponseDTO> response = service.getAllIssues();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getStatus()).isEqualTo("OPEN");
        assertThat(response.get(0).getAssignedTechnicianUserId()).isNull();
        assertThat(response.get(0).getAssignedTechnicianName()).isNull();
    }

    @Test
    void adminCanFilterIssuesByCreatedBy() {
        User resident = user(10L, 4L, true);
        Issue issue = issue(100L, "OPEN", resident);
        TenantContext.set(1L, "tenant", "tenant", 30L, "ROLE_ADMIN");

        when(issueRepository.findByCreatedById(resident.getId())).thenReturn(List.of(issue));
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(issue.getId()))
                .thenReturn(Optional.empty());

        List<IssueResponseDTO> response = service.getIssuesByCreatedBy(resident.getId());

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCreatedById()).isEqualTo(resident.getId());
    }

    private Issue issue(Long id, String status) {
        return issue(id, status, user(6L, 4L, true));
    }

    private Issue issue(Long id, String status, User createdBy) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setTitle("Broken sink");
        issue.setDescription("Water is leaking.");
        issue.setStatus(status);
        issue.setPriority("MEDIUM");
        issue.setApartment(apartment(5L));
        issue.setCreatedBy(createdBy);
        return issue;
    }

    private CreateIssueRequest createIssueRequest(Long apartmentId) {
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("Broken sink");
        request.setDescription("Water is leaking.");
        request.setPriority("MEDIUM");
        request.setApartmentId(apartmentId);
        return request;
    }

    private IssueResponseDTO createIssueWithCandidates(
            String priority,
            List<TechnicianProfile> candidates,
            User selectedTechnician,
            List<IssueAssignment> selectedAssignments,
            List<IssueAssignment> otherAssignments
    ) {
        User staff = user(10L, 2L, true);
        Apartment apartment = apartment(30L);
        IssueCategory plumbing = issueCategory(40L, "Plumbing", "Plumbing");
        CreateIssueRequest request = createIssueRequest(apartment.getId());
        request.setPriority(priority);
        IssueAssignment selectedAssignment = assignment(90L, null, selectedTechnician);
        TenantContext.set(1L, "tenant", "tenant", staff.getId(), "ROLE_STAFF");

        when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
        when(apartmentRepository.findById(apartment.getId())).thenReturn(Optional.of(apartment));
        when(issueCategoryMatcherService.matchCategory(request.getTitle(), request.getDescription()))
                .thenReturn(Optional.of(new IssueCategoryMatch(plumbing, 0.9, "Matched Plumbing.")));
        when(technicianProfileRepository.findByIsAvailableTrue()).thenReturn(candidates);
        when(roleRepository.findById(3L)).thenReturn(Optional.of(role(3L, "ROLE_TECHNICIAN")));

        for (TechnicianProfile candidate : candidates) {
            List<IssueAssignment> assignments = candidate.getUser().getId().equals(selectedTechnician.getId())
                    ? selectedAssignments
                    : otherAssignments;
            when(issueAssignmentRepository.findByTechnicianId(candidate.getUser().getId())).thenReturn(assignments);
        }

        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(70L);
            selectedAssignment.setIssue(issue);
            return issue;
        });
        when(issueAssignmentRepository.save(any(IssueAssignment.class))).thenReturn(selectedAssignment);
        when(issueAssignmentRepository.findTopByIssueIdOrderByAssignedAtDescIdDesc(70L))
                .thenReturn(Optional.of(selectedAssignment));

        return service.createIssue(request);
    }

    private ResidentProfile residentProfile(Long id, User user, Apartment apartment) {
        ResidentProfile profile = new ResidentProfile();
        profile.setId(id);
        profile.setUser(user);
        profile.setApartment(apartment);
        return profile;
    }

    private Apartment apartment(Long id) {
        Apartment apartment = new Apartment();
        apartment.setId(id);
        apartment.setBuilding(building(1L));
        return apartment;
    }

    private Building building(Long id) {
        Building building = new Building();
        building.setId(id);
        building.setName("Building " + id);
        building.setAddress("Main Street " + id);
        return building;
    }

    private User user(Long id, Long roleId, Boolean active) {
        User user = new User();
        user.setId(id);
        user.setRoleId(roleId);
        user.setEmail("user" + id + "@example.com");
        user.setPasswordHash("password");
        user.setIsActive(active);
        return user;
    }

    private Role role(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }

    private IssueCategory issueCategory(Long id, String name, String requiredSpecialization) {
        IssueCategory category = new IssueCategory();
        category.setId(id);
        category.setName(name);
        category.setDescription(name + " issues");
        category.setRequiredSpecialization(requiredSpecialization);
        return category;
    }

    private TechnicianProfile technicianProfile(Long id, User user) {
        return technicianProfile(id, user, null);
    }

    private TechnicianProfile technicianProfile(Long id, User user, String specialization) {
        TechnicianProfile profile = new TechnicianProfile();
        profile.setId(id);
        profile.setUser(user);
        profile.setSpecialization(specialization);
        profile.setIsAvailable(true);
        profile.setMaxActiveIssues(5);
        return profile;
    }

    private Issue workloadIssue(Long id, String status, String priority) {
        Issue issue = issue(id, status);
        issue.setPriority(priority);
        return issue;
    }

    private IssueAssignment assignment(Long id, Issue issue, User technician) {
        IssueAssignment assignment = new IssueAssignment();
        assignment.setId(id);
        assignment.setIssue(issue);
        assignment.setTechnician(technician);
        return assignment;
    }
}
