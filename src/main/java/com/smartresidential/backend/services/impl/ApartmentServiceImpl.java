package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.apartment.ApartmentResponseDTO;
import com.smartresidential.backend.dto.apartment.CreateApartmentRequest;
import com.smartresidential.backend.dto.apartment.UpdateApartmentRequest;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.Building;
import com.smartresidential.backend.repositories.ApartmentRepository;
import com.smartresidential.backend.repositories.BuildingRepository;
import com.smartresidential.backend.services.interfaces.ApartmentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApartmentServiceImpl implements ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;

    public ApartmentServiceImpl(ApartmentRepository apartmentRepository,
                                BuildingRepository buildingRepository) {
        this.apartmentRepository = apartmentRepository;
        this.buildingRepository = buildingRepository;
    }

    @Override
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
    public ApartmentResponseDTO getApartmentById(Long id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apartment not found with id: " + id));

        return mapToDTO(apartment);
    }

    @Override
    public List<ApartmentResponseDTO> getAllApartments() {
        return apartmentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApartmentResponseDTO> getApartmentsByBuildingId(Long buildingId) {
        return apartmentRepository.findByBuildingId(buildingId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ApartmentResponseDTO updateApartment(Long id, UpdateApartmentRequest request) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apartment not found with id: " + id));

        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found with id: " + request.getBuildingId()));

        apartment.setBuilding(building);
        apartment.setUnitNumber(request.getUnitNumber());
        apartment.setFloor(request.getFloor());

        Apartment updatedApartment = apartmentRepository.save(apartment);
        return mapToDTO(updatedApartment);
    }

    @Override
    public void deleteApartment(Long id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apartment not found with id: " + id));

        apartmentRepository.delete(apartment);
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