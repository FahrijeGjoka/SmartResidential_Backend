package com.smartresidential.backend.services;

import com.smartresidential.backend.dto.buildingAnnouncement.BuildingAnnouncementResponseDTO;
import com.smartresidential.backend.dto.buildingAnnouncement.CreateBuildingAnnouncementRequest;
import com.smartresidential.backend.dto.buildingAnnouncement.UpdateBuildingAnnouncementRequest;

import java.util.List;

public interface BuildingAnnouncementService {

    BuildingAnnouncementResponseDTO createBuildingAnnouncement(CreateBuildingAnnouncementRequest request);

    BuildingAnnouncementResponseDTO getBuildingAnnouncementById(Long id);

    List<BuildingAnnouncementResponseDTO> getAllBuildingAnnouncements();

    List<BuildingAnnouncementResponseDTO> getAnnouncementsByBuildingId(Long buildingId);

    BuildingAnnouncementResponseDTO updateBuildingAnnouncement(Long id, UpdateBuildingAnnouncementRequest request);

    void deleteBuildingAnnouncement(Long id);
}