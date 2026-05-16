package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.issue.IssueResponseDTO;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.IssueAssignment;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.TechnicianProfile;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.cache.TenantCacheEvictor;
import com.smartresidential.backend.exceptions.ConflictException;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.jobs.NotificationJob;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.ApartmentRepository;
import com.smartresidential.backend.repositories.IssueAssignmentRepository;
import com.smartresidential.backend.repositories.IssueCategoryRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.IssueStatusHistoryRepository;
import com.smartresidential.backend.repositories.RoleRepository;
import com.smartresidential.backend.repositories.TechnicianProfileRepository;
import com.smartresidential.backend.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private TechnicianProfileRepository technicianProfileRepository;

    @Mock
    private RoleRepository roleRepository;

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
                technicianProfileRepository,
                roleRepository,
                notificationJob,
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
        verify(notificationJob).notifyTechnicianAssigned(issue.getId(), technician.getId());
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

        assertThatThrownBy(() -> service.getIssueById(issue.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Issue is ASSIGNED but has no assigned technician.");
    }

    private Issue issue(Long id, String status) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setTitle("Broken sink");
        issue.setDescription("Water is leaking.");
        issue.setStatus(status);
        issue.setPriority("MEDIUM");
        issue.setApartment(apartment(5L));
        issue.setCreatedBy(user(6L, 4L, true));
        return issue;
    }

    private Apartment apartment(Long id) {
        Apartment apartment = new Apartment();
        apartment.setId(id);
        return apartment;
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

    private TechnicianProfile technicianProfile(Long id, User user) {
        TechnicianProfile profile = new TechnicianProfile();
        profile.setId(id);
        profile.setUser(user);
        profile.setIsAvailable(true);
        return profile;
    }

    private IssueAssignment assignment(Long id, Issue issue, User technician) {
        IssueAssignment assignment = new IssueAssignment();
        assignment.setId(id);
        assignment.setIssue(issue);
        assignment.setTechnician(technician);
        return assignment;
    }
}
