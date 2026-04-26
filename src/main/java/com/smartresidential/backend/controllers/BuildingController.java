package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.building.BuildingResponseDTO;
import com.smartresidential.backend.dto.building.CreateBuildingRequest;
import com.smartresidential.backend.dto.building.UpdateBuildingRequest;
import com.smartresidential.backend.services.interfaces.BuildingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @PostMapping
    public ResponseEntity<BuildingResponseDTO> createBuilding(
            @RequestBody CreateBuildingRequest request
    ) {
        BuildingResponseDTO response = buildingService.createBuilding(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuildingResponseDTO> getBuildingById(
            @PathVariable Long id
    ) {
        BuildingResponseDTO response = buildingService.getBuildingById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BuildingResponseDTO>> getAllBuildings() {
        List<BuildingResponseDTO> response = buildingService.getAllBuildings();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BuildingResponseDTO> updateBuilding(
            @PathVariable Long id,
            @RequestBody UpdateBuildingRequest request
    ) {
        BuildingResponseDTO response = buildingService.updateBuilding(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuilding(
            @PathVariable Long id
    ) {
        buildingService.deleteBuilding(id);
        return ResponseEntity.noContent().build();
    }
}
