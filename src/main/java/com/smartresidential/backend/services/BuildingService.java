package com.smartresidential.backend.services;

import com.smartresidential.backend.dto.building.BuildingResponseDTO;
import com.smartresidential.backend.dto.building.CreateBuildingRequest;
import com.smartresidential.backend.dto.building.UpdateBuildingRequest;

import java.util.List;

public interface BuildingService {

    BuildingResponseDTO createBuilding(CreateBuildingRequest request);

    BuildingResponseDTO getBuildingById(Long id);

    List<BuildingResponseDTO> getAllBuildings();

    BuildingResponseDTO updateBuilding(Long id, UpdateBuildingRequest request);

    void deleteBuilding(Long id);
}