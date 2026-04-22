package com.smartresidential.backend.services;

import com.smartresidential.backend.dto.buildingAnnouncement.BuildingAnnouncementResponseDTO;
import com.smartresidential.backend.dto.buildingAnnouncement.CreateBuildingAnnouncementRequest;
import com.smartresidential.backend.dto.buildingAnnouncement.UpdateBuildingAnnouncementRequest;
import com.smartresidential.backend.entities.Building;
import com.smartresidential.backend.entities.BuildingAnnouncement;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.repositories.BuildingAnnouncementRepository;
import com.smartresidential.backend.repositories.BuildingRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.BuildingAnnouncementService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BuildingAnnouncementServiceImpl implements BuildingAnnouncementService {

    private final BuildingAnnouncementRepository buildingAnnouncementRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;

    public BuildingAnnouncementServiceImpl(BuildingAnnouncementRepository buildingAnnouncementRepository,
                                           BuildingRepository buildingRepository,
                                           UserRepository userRepository) {
        this.buildingAnnouncementRepository = buildingAnnouncementRepository;
        this.buildingRepository = buildingRepository;
        this.userRepository = userRepository;
    }

    @Override
    public BuildingAnnouncementResponseDTO createBuildingAnnouncement(CreateBuildingAnnouncementRequest request) {
        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found with id: " + request.getBuildingId()));

        User createdBy = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getCreatedBy()));

        BuildingAnnouncement announcement = new BuildingAnnouncement();
        announcement.setBuilding(building);
        announcement.setTitle(request.getTitle());
        announcement.setMessage(request.getMessage());
        announcement.setCreatedBy(createdBy);

        BuildingAnnouncement savedAnnouncement = buildingAnnouncementRepository.save(announcement);
        return mapToDTO(savedAnnouncement);
    }

    @Override
    public BuildingAnnouncementResponseDTO getBuildingAnnouncementById(Long id) {
        BuildingAnnouncement announcement = buildingAnnouncementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BuildingAnnouncement not found with id: " + id));

        return mapToDTO(announcement);
    }

    @Override
    public List<BuildingAnnouncementResponseDTO> getAllBuildingAnnouncements() {
        return buildingAnnouncementRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BuildingAnnouncementResponseDTO> getAnnouncementsByBuildingId(Long buildingId) {
        return buildingAnnouncementRepository.findByBuildingId(buildingId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BuildingAnnouncementResponseDTO updateBuildingAnnouncement(Long id, UpdateBuildingAnnouncementRequest request) {
        BuildingAnnouncement announcement = buildingAnnouncementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BuildingAnnouncement not found with id: " + id));

        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found with id: " + request.getBuildingId()));

        User createdBy = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getCreatedBy()));

        announcement.setBuilding(building);
        announcement.setTitle(request.getTitle());
        announcement.setMessage(request.getMessage());
        announcement.setCreatedBy(createdBy);

        BuildingAnnouncement updatedAnnouncement = buildingAnnouncementRepository.save(announcement);
        return mapToDTO(updatedAnnouncement);
    }

    @Override
    public void deleteBuildingAnnouncement(Long id) {
        BuildingAnnouncement announcement = buildingAnnouncementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BuildingAnnouncement not found with id: " + id));

        buildingAnnouncementRepository.delete(announcement);
    }

    private BuildingAnnouncementResponseDTO mapToDTO(BuildingAnnouncement announcement) {
        return new BuildingAnnouncementResponseDTO(
                announcement.getId(),
                announcement.getBuilding().getId(),
                announcement.getTitle(),
                announcement.getMessage(),
                announcement.getCreatedBy().getId(),
                announcement.getCreatedAt()
        );
    }
}