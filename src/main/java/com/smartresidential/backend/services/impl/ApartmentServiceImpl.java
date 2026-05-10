package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.apartment.ApartmentResponseDTO;
import com.smartresidential.backend.dto.apartment.CreateApartmentRequest;
import com.smartresidential.backend.dto.apartment.UpdateApartmentRequest;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.Building;
import com.smartresidential.backend.repositories.ApartmentRepository;
import com.smartresidential.backend.repositories.BuildingRepository;
import com.smartresidential.backend.services.interfaces.ApartmentService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApartmentServiceImpl implements ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;
    private final CacheManager cacheManager;

    public ApartmentServiceImpl(ApartmentRepository apartmentRepository,
                                BuildingRepository buildingRepository,
                                CacheManager cacheManager) {
        this.apartmentRepository = apartmentRepository;
        this.buildingRepository = buildingRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = "apartments",
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).all('apartments')"
            ),
            @CacheEvict(
                    cacheNames = "apartments",
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).byBuildingId('apartments', #request.buildingId)"
            )
    })
    public ApartmentResponseDTO createApartment(CreateApartmentRequest request) {
        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found with id: " + request.getBuildingId()));

        Apartment apartment = new Apartment();
        apartment.setBuilding(building);
        apartment.setUnitNumber(request.getUnitNumber());
        apartment.setFloor(request.getFloor());

        Apartment savedApartment = apartmentRepository.save(apartment);
        return mapToDTO(savedApartment);
    }

    @Override
    @Cacheable(
            cacheNames = "apartments",
            key = "T(com.smartresidential.backend.cache.TenantCacheKeys).byId('apartments', #id)"
    )
    public ApartmentResponseDTO getApartmentById(Long id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apartment not found with id: " + id));

        return mapToDTO(apartment);
    }

    @Override
    @Cacheable(
            cacheNames = "apartments",
            key = "T(com.smartresidential.backend.cache.TenantCacheKeys).all('apartments')"
    )
    public List<ApartmentResponseDTO> getAllApartments() {
        return apartmentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(
            cacheNames = "apartments",
            key = "T(com.smartresidential.backend.cache.TenantCacheKeys).byBuildingId('apartments', #buildingId)"
    )
    public List<ApartmentResponseDTO> getApartmentsByBuildingId(Long buildingId) {
        return apartmentRepository.findByBuildingId(buildingId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = "apartments",
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).all('apartments')"
            ),
            @CacheEvict(
                    cacheNames = "apartments",
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).byId('apartments', #id)"
            ),
            @CacheEvict(
                    cacheNames = "apartments",
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).byBuildingId('apartments', #request.buildingId)"
            )
    })
    public ApartmentResponseDTO updateApartment(Long id, UpdateApartmentRequest request) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apartment not found with id: " + id));
        Long previousBuildingId = apartment.getBuilding().getId();

        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found with id: " + request.getBuildingId()));

        apartment.setBuilding(building);
        apartment.setUnitNumber(request.getUnitNumber());
        apartment.setFloor(request.getFloor());

        Apartment updatedApartment = apartmentRepository.save(apartment);
        evictApartmentsByBuilding(previousBuildingId);
        return mapToDTO(updatedApartment);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = "apartments",
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).all('apartments')"
            ),
            @CacheEvict(
                    cacheNames = "apartments",
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).byId('apartments', #id)"
            )
    })
    public void deleteApartment(Long id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apartment not found with id: " + id));
        Long buildingId = apartment.getBuilding().getId();

        apartmentRepository.delete(apartment);
        evictApartmentsByBuilding(buildingId);
    }

    private void evictApartmentsByBuilding(Long buildingId) {
        Cache cache = cacheManager.getCache("apartments");
        if (cache != null) {
            cache.evict(com.smartresidential.backend.cache.TenantCacheKeys.byBuildingId("apartments", buildingId));
        }
    }

    private ApartmentResponseDTO mapToDTO(Apartment apartment) {
        return new ApartmentResponseDTO(
                apartment.getId(),
                apartment.getBuilding().getId(),
                apartment.getUnitNumber(),
                apartment.getFloor(),
                apartment.getCreatedAt()
        );
    }
}
