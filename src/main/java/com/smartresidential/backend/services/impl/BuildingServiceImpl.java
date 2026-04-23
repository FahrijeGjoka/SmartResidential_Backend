package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.building.BuildingResponseDTO;
import com.smartresidential.backend.dto.building.CreateBuildingRequest;
import com.smartresidential.backend.dto.building.UpdateBuildingRequest;
import com.smartresidential.backend.entities.Building;
import com.smartresidential.backend.repositories.BuildingRepository;
import com.smartresidential.backend.services.interfaces.BuildingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;

    public BuildingServiceImpl(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @Override
    public BuildingResponseDTO createBuilding(CreateBuildingRequest request) {
        Building building = new Building();
        building.setName(request.getName());
        building.setAddress(request.getAddress());

        Building savedBuilding = buildingRepository.save(building);
        return mapToDTO(savedBuilding);
    }

    @Override
    public BuildingResponseDTO getBuildingById(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found with id: " + id));

        return mapToDTO(building);
    }

    @Override
    public List<BuildingResponseDTO> getAllBuildings() {
        return buildingRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BuildingResponseDTO updateBuilding(Long id, UpdateBuildingRequest request) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found with id: " + id));

        building.setName(request.getName());
        building.setAddress(request.getAddress());

        Building updatedBuilding = buildingRepository.save(building);
        return mapToDTO(updatedBuilding);
    }

    @Override
    public void deleteBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found with id: " + id));

        buildingRepository.delete(building);
    }

    private BuildingResponseDTO mapToDTO(Building building) {
        return new BuildingResponseDTO(
                building.getId(),
                building.getName(),
                building.getAddress(),
                building.getCreatedAt()
        );
    }
}