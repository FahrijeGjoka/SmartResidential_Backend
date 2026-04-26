package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.residentProfile.CreateResidentProfileRequest;
import com.smartresidential.backend.dto.residentProfile.ResidentProfileResponseDTO;
import com.smartresidential.backend.dto.residentProfile.UpdateResidentProfileRequest;
import com.smartresidential.backend.services.interfaces.ResidentProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resident-profiles")
public class ResidentProfileController {

    private final ResidentProfileService residentProfileService;

    public ResidentProfileController(ResidentProfileService residentProfileService) {
        this.residentProfileService = residentProfileService;
    }

    @PostMapping
    public ResponseEntity<ResidentProfileResponseDTO> createResidentProfile(
            @RequestBody CreateResidentProfileRequest request
    ) {
        ResidentProfileResponseDTO response =
                residentProfileService.createResidentProfile(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResidentProfileResponseDTO> getResidentProfileById(
            @PathVariable Long id
    ) {
        ResidentProfileResponseDTO response =
                residentProfileService.getResidentProfileById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ResidentProfileResponseDTO>> getAllResidentProfiles() {
        List<ResidentProfileResponseDTO> response =
                residentProfileService.getAllResidentProfiles();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/building/{buildingId}")
    public ResponseEntity<List<ResidentProfileResponseDTO>> getResidentProfilesByBuildingId(
            @PathVariable Long buildingId
    ) {
        List<ResidentProfileResponseDTO> response =
                residentProfileService.getResidentProfilesByBuildingId(buildingId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResidentProfileResponseDTO> updateResidentProfile(
            @PathVariable Long id,
            @RequestBody UpdateResidentProfileRequest request
    ) {
        ResidentProfileResponseDTO response =
                residentProfileService.updateResidentProfile(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResidentProfile(
            @PathVariable Long id
    ) {
        residentProfileService.deleteResidentProfile(id);
        return ResponseEntity.noContent().build();
    }
}