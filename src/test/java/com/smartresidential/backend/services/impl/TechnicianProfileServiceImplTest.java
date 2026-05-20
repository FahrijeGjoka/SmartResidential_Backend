package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.technicianProfile.TechnicianProfileResponseDTO;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.IssueAssignment;
import com.smartresidential.backend.entities.TechnicianProfile;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.repositories.IssueAssignmentRepository;
import com.smartresidential.backend.repositories.TechnicianProfileRepository;
import com.smartresidential.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnicianProfileServiceImplTest {

    @Mock
    private TechnicianProfileRepository technicianProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IssueAssignmentRepository issueAssignmentRepository;

    @Test
    void responseIncludesManualAvailabilityAndCalculatedWorkload() {
        TechnicianProfileServiceImpl service = service();
        User technician = user(20L);
        TechnicianProfile profile = technicianProfile(50L, technician);
        LocalDateTime older = LocalDateTime.now().minusDays(2);
        LocalDateTime latest = LocalDateTime.now().minusHours(1);
        LocalDateTime mostRecent = LocalDateTime.now();

        IssueAssignment lowActive = assignment(1L, issue(10L, "ASSIGNED", "LOW"), technician, older);
        IssueAssignment urgentActive = assignment(2L, issue(11L, "IN_PROGRESS", "URGENT"), technician, latest);
        IssueAssignment closed = assignment(3L, issue(12L, "CLOSED", "HIGH"), technician, mostRecent);

        when(technicianProfileRepository.findById(profile.getId())).thenReturn(Optional.of(profile));
        when(issueAssignmentRepository.findByTechnicianId(technician.getId()))
                .thenReturn(List.of(lowActive, urgentActive, closed));

        TechnicianProfileResponseDTO response = service.getById(profile.getId());

        assertThat(response.getIsAvailable()).isTrue();
        assertThat(response.getMaxActiveIssues()).isEqualTo(5);
        assertThat(response.getActiveIssueCount()).isEqualTo(2);
        assertThat(response.getActiveHighPriorityIssueCount()).isEqualTo(1);
        assertThat(response.getLastAssignedAt()).isEqualTo(mostRecent);
    }

    @Test
    void technicianWithNoAssignmentsReturnsZeroCountsAndNullLastAssignedAt() {
        TechnicianProfileServiceImpl service = service();
        User technician = user(20L);
        TechnicianProfile profile = technicianProfile(50L, technician);

        when(technicianProfileRepository.findById(profile.getId())).thenReturn(Optional.of(profile));
        when(issueAssignmentRepository.findByTechnicianId(technician.getId())).thenReturn(List.of());

        TechnicianProfileResponseDTO response = service.getById(profile.getId());

        assertThat(response.getActiveIssueCount()).isZero();
        assertThat(response.getActiveHighPriorityIssueCount()).isZero();
        assertThat(response.getLastAssignedAt()).isNull();
    }

    @Test
    void lastAssignedAtFallsBackToIssueUpdatedAtWhenAssignmentTimestampIsMissing() {
        TechnicianProfileServiceImpl service = service();
        User technician = user(20L);
        TechnicianProfile profile = technicianProfile(50L, technician);
        Issue issue = issue(10L, "ASSIGNED", "HIGH");
        LocalDateTime updatedAt = LocalDateTime.now().minusMinutes(15);
        issue.setCreatedAt(LocalDateTime.now().minusHours(2));
        issue.setUpdatedAt(updatedAt);
        IssueAssignment assignment = assignment(1L, issue, technician, null);

        when(technicianProfileRepository.findById(profile.getId())).thenReturn(Optional.of(profile));
        when(issueAssignmentRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(assignment));

        TechnicianProfileResponseDTO response = service.getById(profile.getId());

        assertThat(response.getLastAssignedAt()).isEqualTo(updatedAt);
    }

    @Test
    void getAllReturnsWorkloadFieldsForAvailableAndUnavailableTechnicians() {
        TechnicianProfileServiceImpl service = service();
        User availableUser = user(20L);
        User unavailableUser = user(21L);
        TechnicianProfile available = technicianProfile(50L, availableUser);
        TechnicianProfile unavailable = technicianProfile(51L, unavailableUser);
        unavailable.setIsAvailable(false);

        when(technicianProfileRepository.findAll()).thenReturn(List.of(available, unavailable));
        when(issueAssignmentRepository.findByTechnicianId(availableUser.getId()))
                .thenReturn(List.of(assignment(1L, issue(10L, "ASSIGNED", "LOW"), availableUser, LocalDateTime.now())));
        when(issueAssignmentRepository.findByTechnicianId(unavailableUser.getId()))
                .thenReturn(List.of(assignment(2L, issue(11L, "IN_PROGRESS", "HIGH"), unavailableUser, LocalDateTime.now())));

        List<TechnicianProfileResponseDTO> response = service.getAll();

        assertThat(response).hasSize(2);
        assertThat(response).extracting(TechnicianProfileResponseDTO::getActiveIssueCount)
                .containsExactly(1, 1);
        assertThat(response).extracting(TechnicianProfileResponseDTO::getIsAvailable)
                .containsExactly(true, false);
    }

    @Test
    void getAvailableReturnsOnlyAvailableTechniciansWithWorkloadFields() {
        TechnicianProfileServiceImpl service = service();
        User technician = user(20L);
        TechnicianProfile available = technicianProfile(50L, technician);

        when(technicianProfileRepository.findByIsAvailableTrue()).thenReturn(List.of(available));
        when(issueAssignmentRepository.findByTechnicianId(technician.getId()))
                .thenReturn(List.of(assignment(1L, issue(10L, "IN_PROGRESS", "URGENT"), technician, LocalDateTime.now())));

        List<TechnicianProfileResponseDTO> response = service.getAvailable();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getIsAvailable()).isTrue();
        assertThat(response.get(0).getActiveIssueCount()).isEqualTo(1);
        assertThat(response.get(0).getActiveHighPriorityIssueCount()).isEqualTo(1);
        assertThat(response.get(0).getLastAssignedAt()).isNotNull();
    }

    @Test
    void getAvailableExcludesTechniciansAtCapacity() {
        TechnicianProfileServiceImpl service = service();
        User underCapacityUser = user(20L);
        User atCapacityUser = user(21L);
        TechnicianProfile underCapacity = technicianProfile(50L, underCapacityUser);
        TechnicianProfile atCapacity = technicianProfile(51L, atCapacityUser);

        when(technicianProfileRepository.findByIsAvailableTrue())
                .thenReturn(List.of(underCapacity, atCapacity));
        when(issueAssignmentRepository.findByTechnicianId(underCapacityUser.getId()))
                .thenReturn(activeAssignments(underCapacityUser, 4));
        when(issueAssignmentRepository.findByTechnicianId(atCapacityUser.getId()))
                .thenReturn(activeAssignments(atCapacityUser, 5));

        List<TechnicianProfileResponseDTO> response = service.getAvailable();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getUserId()).isEqualTo(underCapacityUser.getId());
        assertThat(response.get(0).getActiveIssueCount()).isEqualTo(4);
        assertThat(response.get(0).getMaxActiveIssues()).isEqualTo(5);
    }

    @Test
    void resolvedAndClosedIssuesAreNotCountedAsActive() {
        TechnicianProfileServiceImpl service = service();
        User technician = user(20L);
        TechnicianProfile profile = technicianProfile(50L, technician);

        when(technicianProfileRepository.findById(profile.getId())).thenReturn(Optional.of(profile));
        when(issueAssignmentRepository.findByTechnicianId(technician.getId()))
                .thenReturn(List.of(
                        assignment(1L, issue(10L, "RESOLVED", "HIGH"), technician, LocalDateTime.now()),
                        assignment(2L, issue(11L, "CLOSED", "URGENT"), technician, LocalDateTime.now())
                ));

        TechnicianProfileResponseDTO response = service.getById(profile.getId());

        assertThat(response.getActiveIssueCount()).isZero();
        assertThat(response.getActiveHighPriorityIssueCount()).isZero();
    }

    private TechnicianProfileServiceImpl service() {
        return new TechnicianProfileServiceImpl(
                technicianProfileRepository,
                userRepository,
                issueAssignmentRepository
        );
    }

    private TechnicianProfile technicianProfile(Long id, User user) {
        TechnicianProfile profile = new TechnicianProfile();
        profile.setId(id);
        profile.setUser(user);
        profile.setSpecialization("Plumbing");
        profile.setIsAvailable(true);
        profile.setMaxActiveIssues(5);
        return profile;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("tech@example.com");
        user.setIsActive(true);
        return user;
    }

    private Issue issue(Long id, String status, String priority) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setStatus(status);
        issue.setPriority(priority);
        return issue;
    }

    private IssueAssignment assignment(Long id, Issue issue, User technician, LocalDateTime assignedAt) {
        IssueAssignment assignment = new IssueAssignment();
        assignment.setId(id);
        assignment.setIssue(issue);
        assignment.setTechnician(technician);
        assignment.setAssignedAt(assignedAt);
        return assignment;
    }

    private List<IssueAssignment> activeAssignments(User technician, int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> assignment(
                        1_000L + index,
                        issue(2_000L + index, "ASSIGNED", "MEDIUM"),
                        technician,
                        LocalDateTime.now().minusMinutes(index)
                ))
                .toList();
    }
}
