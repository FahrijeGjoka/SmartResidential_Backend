package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.buildingAnnouncement.BuildingAnnouncementResponseDTO;
import com.smartresidential.backend.dto.buildingAnnouncement.CreateBuildingAnnouncementRequest;
import com.smartresidential.backend.dto.buildingAnnouncement.UpdateBuildingAnnouncementRequest;
import com.smartresidential.backend.services.interfaces.BuildingAnnouncementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final BuildingAnnouncementService buildingAnnouncementService;

    public AnnouncementController(BuildingAnnouncementService buildingAnnouncementService) {
        this.buildingAnnouncementService = buildingAnnouncementService;
    }

    @PostMapping
    public ResponseEntity<BuildingAnnouncementResponseDTO> createAnnouncement(
            @RequestBody CreateBuildingAnnouncementRequest request
    ) {
        BuildingAnnouncementResponseDTO response =
                buildingAnnouncementService.createBuildingAnnouncement(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuildingAnnouncementResponseDTO> getAnnouncementById(
            @PathVariable Long id
    ) {
        BuildingAnnouncementResponseDTO response =
                buildingAnnouncementService.getBuildingAnnouncementById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BuildingAnnouncementResponseDTO>> getAllAnnouncements() {
        List<BuildingAnnouncementResponseDTO> response =
                buildingAnnouncementService.getAllBuildingAnnouncements();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/building/{buildingId}")
    public ResponseEntity<List<BuildingAnnouncementResponseDTO>> getAnnouncementsByBuildingId(
            @PathVariable Long buildingId
    ) {
        List<BuildingAnnouncementResponseDTO> response =
                buildingAnnouncementService.getAnnouncementsByBuildingId(buildingId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BuildingAnnouncementResponseDTO> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody UpdateBuildingAnnouncementRequest request
    ) {
        BuildingAnnouncementResponseDTO response =
                buildingAnnouncementService.updateBuildingAnnouncement(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long id
    ) {
        buildingAnnouncementService.deleteBuildingAnnouncement(id);
        return ResponseEntity.noContent().build();
    }
}