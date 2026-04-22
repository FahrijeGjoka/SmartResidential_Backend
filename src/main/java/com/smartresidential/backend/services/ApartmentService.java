package com.smartresidential.backend.services;

import com.smartresidential.backend.dto.apartment.ApartmentResponseDTO;
import com.smartresidential.backend.dto.apartment.CreateApartmentRequest;
import com.smartresidential.backend.dto.apartment.UpdateApartmentRequest;

import java.util.List;

public interface ApartmentService {

    ApartmentResponseDTO createApartment(CreateApartmentRequest request);

    ApartmentResponseDTO getApartmentById(Long id);

    List<ApartmentResponseDTO> getAllApartments();

    List<ApartmentResponseDTO> getApartmentsByBuildingId(Long buildingId);

    ApartmentResponseDTO updateApartment(Long id, UpdateApartmentRequest request);

    void deleteApartment(Long id);
}