package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.cache.CacheNames;
import com.smartresidential.backend.dto.building.BuildingFilterRequest;
import com.smartresidential.backend.dto.building.BuildingResponseDTO;
import com.smartresidential.backend.dto.building.CreateBuildingRequest;
import com.smartresidential.backend.dto.building.UpdateBuildingRequest;
import com.smartresidential.backend.dto.common.PageRequestFactory;
import com.smartresidential.backend.entities.Building;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.repositories.BuildingRepository;
import com.smartresidential.backend.services.interfaces.BuildingService;
import com.smartresidential.backend.specifications.BuildingSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
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
    @CacheEvict(
            cacheNames = CacheNames.BUILDINGS,
            key = "T(com.smartresidential.backend.cache.TenantCacheKeys).all('buildings')"
    )
    public BuildingResponseDTO createBuilding(CreateBuildingRequest request) {
        Building building = new Building();
        building.setName(request.getName());
        building.setAddress(request.getAddress());

        Building savedBuilding = buildingRepository.save(building);
        return mapToDTO(savedBuilding);
    }

    @Override
    @Cacheable(
            cacheNames = CacheNames.BUILDINGS,
            key = "T(com.smartresidential.backend.cache.TenantCacheKeys).byId('buildings', #id)"
    )
    public BuildingResponseDTO getBuildingById(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));

        return mapToDTO(building);
    }

    @Override
    @Cacheable(
            cacheNames = CacheNames.BUILDINGS,
            key = "T(com.smartresidential.backend.cache.TenantCacheKeys).all('buildings')"
    )
    public List<BuildingResponseDTO> getAllBuildings() {
        return buildingRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BuildingResponseDTO> searchBuildings(BuildingFilterRequest filter) {
        return buildingRepository.findAll(
                BuildingSpecification.withFilters(filter),
                PageRequestFactory.from(filter, "createdAt")
        ).map(this::mapToDTO);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = CacheNames.BUILDINGS,
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).all('buildings')"
            ),
            @CacheEvict(
                    cacheNames = CacheNames.BUILDINGS,
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).byId('buildings', #id)"
            )
    })
    public BuildingResponseDTO updateBuilding(Long id, UpdateBuildingRequest request) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));

        building.setName(request.getName());
        building.setAddress(request.getAddress());

        Building updatedBuilding = buildingRepository.save(building);
        return mapToDTO(updatedBuilding);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = CacheNames.BUILDINGS,
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).all('buildings')"
            ),
            @CacheEvict(
                    cacheNames = CacheNames.BUILDINGS,
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).byId('buildings', #id)"
            )
    })
    public void deleteBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));

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
