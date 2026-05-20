package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.dashboard.ResidentDashboardAnnouncement;
import com.smartresidential.backend.dto.dashboard.ResidentDashboardIssue;
import com.smartresidential.backend.dto.dashboard.ResidentDashboardResponse;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.BuildingAnnouncement;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.ResidentProfile;
import com.smartresidential.backend.exceptions.UnauthorizedException;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.BuildingAnnouncementRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.NotificationRepository;
import com.smartresidential.backend.repositories.ResidentProfileRepository;
import com.smartresidential.backend.services.interfaces.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final List<String> ACTIVE_ISSUE_STATUSES = List.of("OPEN", "ASSIGNED", "IN_PROGRESS");

    private final IssueRepository issueRepository;
    private final NotificationRepository notificationRepository;
    private final ResidentProfileRepository residentProfileRepository;
    private final BuildingAnnouncementRepository buildingAnnouncementRepository;

    public DashboardServiceImpl(IssueRepository issueRepository,
                                NotificationRepository notificationRepository,
                                ResidentProfileRepository residentProfileRepository,
                                BuildingAnnouncementRepository buildingAnnouncementRepository) {
        this.issueRepository = issueRepository;
        this.notificationRepository = notificationRepository;
        this.residentProfileRepository = residentProfileRepository;
        this.buildingAnnouncementRepository = buildingAnnouncementRepository;
    }

    @Override
    public ResidentDashboardResponse getResidentDashboard() {
        Long residentUserId = TenantContext.getUserId();
        if (residentUserId == null) {
            throw new UnauthorizedException("Authenticated user is required.");
        }

        Optional<ResidentProfile> residentProfile = residentProfileRepository.findByUserId(residentUserId);
        if (residentProfile.isEmpty() || !hasLinkedApartment(residentProfile.get())) {
            return new ResidentDashboardResponse();
        }

        ResidentDashboardResponse response = new ResidentDashboardResponse();
        response.setOpenIssueCount(issueRepository.countByCreatedByIdAndStatusInAndArchivedFalse(
                residentUserId,
                ACTIVE_ISSUE_STATUSES
        ));
        response.setUnreadNotificationCount(notificationRepository.countByUserIdAndIsReadFalse(residentUserId));
        response.setLatestIssue(issueRepository.findTopByCreatedByIdAndArchivedFalseOrderByUpdatedAtDescIdDesc(residentUserId)
                .map(this::mapIssue)
                .orElse(null));

        Long buildingId = residentProfile.get().getApartment().getBuilding().getId();
        response.setLatestAnnouncement(buildingAnnouncementRepository
                .findTopByBuildingIdOrderByCreatedAtDescIdDesc(buildingId)
                .map(this::mapAnnouncement)
                .orElse(null));

        return response;
    }

    private boolean hasLinkedApartment(ResidentProfile residentProfile) {
        Apartment apartment = residentProfile.getApartment();
        return apartment != null && apartment.getBuilding() != null && apartment.getBuilding().getId() != null;
    }

    private ResidentDashboardIssue mapIssue(Issue issue) {
        return new ResidentDashboardIssue(
                issue.getId(),
                issue.getTitle(),
                issue.getStatus(),
                issue.getUpdatedAt()
        );
    }

    private ResidentDashboardAnnouncement mapAnnouncement(BuildingAnnouncement announcement) {
        return new ResidentDashboardAnnouncement(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getCreatedAt()
        );
    }
}
