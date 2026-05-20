package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.dashboard.ResidentDashboardResponse;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.Building;
import com.smartresidential.backend.entities.BuildingAnnouncement;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.ResidentProfile;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.UnauthorizedException;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.BuildingAnnouncementRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.NotificationRepository;
import com.smartresidential.backend.repositories.ResidentProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ResidentProfileRepository residentProfileRepository;

    @Mock
    private BuildingAnnouncementRepository buildingAnnouncementRepository;

    private DashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DashboardServiceImpl(
                issueRepository,
                notificationRepository,
                residentProfileRepository,
                buildingAnnouncementRepository
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void residentDashboardContainsOnlyAuthenticatedResidentsIssueAndNotificationCounts() {
        User resident = user(10L);
        ResidentProfile profile = residentProfile(100L, resident, apartment(20L, building(30L)));
        Issue latestIssue = issue(40L, resident, "Bathroom leak", "IN_PROGRESS",
                LocalDateTime.of(2026, 5, 17, 10, 30));
        BuildingAnnouncement announcement = announcement(50L, profile.getApartment().getBuilding(),
                "Water maintenance", LocalDateTime.of(2026, 5, 17, 9, 0));
        TenantContext.set(1L, "tenant_alpha", "alpha", resident.getId(), "ROLE_RESIDENT");

        when(residentProfileRepository.findByUserId(resident.getId())).thenReturn(Optional.of(profile));
        when(issueRepository.countByCreatedByIdAndStatusInAndArchivedFalse(
                resident.getId(),
                List.of("OPEN", "ASSIGNED", "IN_PROGRESS")
        )).thenReturn(2L);
        when(issueRepository.findTopByCreatedByIdAndArchivedFalseOrderByUpdatedAtDescIdDesc(resident.getId()))
                .thenReturn(Optional.of(latestIssue));
        when(notificationRepository.countByUserIdAndIsReadFalse(resident.getId())).thenReturn(3L);
        when(buildingAnnouncementRepository.findTopByBuildingIdOrderByCreatedAtDescIdDesc(30L))
                .thenReturn(Optional.of(announcement));

        ResidentDashboardResponse response = service.getResidentDashboard();

        assertThat(response.getOpenIssueCount()).isEqualTo(2);
        assertThat(response.getLatestIssue().getId()).isEqualTo(latestIssue.getId());
        assertThat(response.getLatestIssue().getTitle()).isEqualTo("Bathroom leak");
        assertThat(response.getLatestIssue().getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(response.getUnreadNotificationCount()).isEqualTo(3);
        assertThat(response.getLatestAnnouncement().getId()).isEqualTo(announcement.getId());
        assertThat(response.getLatestAnnouncement().getTitle()).isEqualTo("Water maintenance");
        verify(issueRepository, never()).countByCreatedByIdAndStatusInAndArchivedFalse(
                11L,
                List.of("OPEN", "ASSIGNED", "IN_PROGRESS")
        );
        verify(notificationRepository, never()).countByUserIdAndIsReadFalse(11L);
    }

    @Test
    void residentDashboardReturnsNullLatestItemsWhenThereIsNoRecentData() {
        User resident = user(10L);
        ResidentProfile profile = residentProfile(100L, resident, apartment(20L, building(30L)));
        TenantContext.set(1L, "tenant_alpha", "alpha", resident.getId(), "ROLE_RESIDENT");

        when(residentProfileRepository.findByUserId(resident.getId())).thenReturn(Optional.of(profile));
        when(issueRepository.countByCreatedByIdAndStatusInAndArchivedFalse(
                resident.getId(),
                List.of("OPEN", "ASSIGNED", "IN_PROGRESS")
        )).thenReturn(0L);
        when(issueRepository.findTopByCreatedByIdAndArchivedFalseOrderByUpdatedAtDescIdDesc(resident.getId()))
                .thenReturn(Optional.empty());
        when(notificationRepository.countByUserIdAndIsReadFalse(resident.getId())).thenReturn(0L);
        when(buildingAnnouncementRepository.findTopByBuildingIdOrderByCreatedAtDescIdDesc(30L))
                .thenReturn(Optional.empty());

        ResidentDashboardResponse response = service.getResidentDashboard();

        assertThat(response.getOpenIssueCount()).isZero();
        assertThat(response.getLatestIssue()).isNull();
        assertThat(response.getUnreadNotificationCount()).isZero();
        assertThat(response.getLatestAnnouncement()).isNull();
    }

    @Test
    void residentDashboardWithoutProfileReturnsSafeEmptyResponse() {
        TenantContext.set(1L, "tenant_alpha", "alpha", 10L, "ROLE_RESIDENT");

        when(residentProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());

        ResidentDashboardResponse response = service.getResidentDashboard();

        assertThat(response.getOpenIssueCount()).isZero();
        assertThat(response.getLatestIssue()).isNull();
        assertThat(response.getUnreadNotificationCount()).isZero();
        assertThat(response.getLatestAnnouncement()).isNull();
        verify(issueRepository, never()).countByCreatedByIdAndStatusInAndArchivedFalse(
                10L,
                List.of("OPEN", "ASSIGNED", "IN_PROGRESS")
        );
        verify(notificationRepository, never()).countByUserIdAndIsReadFalse(10L);
    }

    @Test
    void residentDashboardWithoutLinkedApartmentReturnsSafeEmptyResponse() {
        User resident = user(10L);
        TenantContext.set(1L, "tenant_alpha", "alpha", resident.getId(), "ROLE_RESIDENT");

        when(residentProfileRepository.findByUserId(resident.getId()))
                .thenReturn(Optional.of(residentProfile(100L, resident, null)));

        ResidentDashboardResponse response = service.getResidentDashboard();

        assertThat(response.getOpenIssueCount()).isZero();
        assertThat(response.getLatestIssue()).isNull();
        assertThat(response.getUnreadNotificationCount()).isZero();
        assertThat(response.getLatestAnnouncement()).isNull();
        verify(issueRepository, never()).findTopByCreatedByIdAndArchivedFalseOrderByUpdatedAtDescIdDesc(resident.getId());
        verify(notificationRepository, never()).countByUserIdAndIsReadFalse(resident.getId());
    }

    @Test
    void residentDashboardDoesNotQueryOtherResidentDataOrOtherBuildingAnnouncements() {
        User resident = user(10L);
        ResidentProfile profile = residentProfile(100L, resident, apartment(20L, building(30L)));
        TenantContext.set(1L, "tenant_alpha", "alpha", resident.getId(), "ROLE_RESIDENT");

        when(residentProfileRepository.findByUserId(resident.getId())).thenReturn(Optional.of(profile));
        when(issueRepository.countByCreatedByIdAndStatusInAndArchivedFalse(
                resident.getId(),
                List.of("OPEN", "ASSIGNED", "IN_PROGRESS")
        )).thenReturn(1L);
        when(issueRepository.findTopByCreatedByIdAndArchivedFalseOrderByUpdatedAtDescIdDesc(resident.getId()))
                .thenReturn(Optional.empty());
        when(notificationRepository.countByUserIdAndIsReadFalse(resident.getId())).thenReturn(1L);
        when(buildingAnnouncementRepository.findTopByBuildingIdOrderByCreatedAtDescIdDesc(30L))
                .thenReturn(Optional.empty());

        service.getResidentDashboard();

        verify(issueRepository, never()).findTopByCreatedByIdAndArchivedFalseOrderByUpdatedAtDescIdDesc(11L);
        verify(notificationRepository, never()).countByUserIdAndIsReadFalse(11L);
        verify(buildingAnnouncementRepository, never()).findTopByBuildingIdOrderByCreatedAtDescIdDesc(31L);
    }

    @Test
    void residentDashboardRequiresAuthenticatedUser() {
        TenantContext.clear();

        assertThatThrownBy(() -> service.getResidentDashboard())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Authenticated user is required.");
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setRoleId(4L);
        user.setEmail("resident" + id + "@example.com");
        user.setPasswordHash("password");
        user.setIsActive(true);
        return user;
    }

    private ResidentProfile residentProfile(Long id, User user, Apartment apartment) {
        ResidentProfile profile = new ResidentProfile();
        profile.setId(id);
        profile.setUser(user);
        profile.setApartment(apartment);
        return profile;
    }

    private Apartment apartment(Long id, Building building) {
        Apartment apartment = new Apartment();
        apartment.setId(id);
        apartment.setBuilding(building);
        apartment.setUnitNumber("A-" + id);
        return apartment;
    }

    private Building building(Long id) {
        Building building = new Building();
        building.setId(id);
        building.setName("Building " + id);
        building.setAddress("Main Street " + id);
        return building;
    }

    private Issue issue(Long id, User createdBy, String title, String status, LocalDateTime updatedAt) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setCreatedBy(createdBy);
        issue.setApartment(apartment(20L, building(30L)));
        issue.setTitle(title);
        issue.setStatus(status);
        issue.setPriority("MEDIUM");
        issue.setUpdatedAt(updatedAt);
        return issue;
    }

    private BuildingAnnouncement announcement(Long id, Building building, String title, LocalDateTime createdAt) {
        BuildingAnnouncement announcement = new BuildingAnnouncement();
        announcement.setId(id);
        announcement.setBuilding(building);
        announcement.setTitle(title);
        announcement.setMessage("Message");
        announcement.setCreatedBy(user(99L));
        announcement.setCreatedAt(createdAt);
        return announcement;
    }
}
