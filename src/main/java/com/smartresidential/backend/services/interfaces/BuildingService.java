package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.building.BuildingFilterRequest;
import com.smartresidential.backend.dto.building.BuildingResponseDTO;
import com.smartresidential.backend.dto.building.CreateBuildingRequest;
import com.smartresidential.backend.dto.building.UpdateBuildingRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BuildingService {

    BuildingResponseDTO createBuilding(CreateBuildingRequest request);

    BuildingResponseDTO getBuildingById(Long id);

    List<BuildingResponseDTO> getAllBuildings();

    Page<BuildingResponseDTO> searchBuildings(BuildingFilterRequest filter);

    BuildingResponseDTO updateBuilding(Long id, UpdateBuildingRequest request);

    void deleteBuilding(Long id);
}
