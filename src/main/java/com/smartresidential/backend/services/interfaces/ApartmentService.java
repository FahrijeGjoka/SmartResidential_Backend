package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.apartment.ApartmentFilterRequest;
import com.smartresidential.backend.dto.apartment.ApartmentResponseDTO;
import com.smartresidential.backend.dto.apartment.CreateApartmentRequest;
import com.smartresidential.backend.dto.apartment.UpdateApartmentRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ApartmentService {

    ApartmentResponseDTO createApartment(CreateApartmentRequest request);

    ApartmentResponseDTO getApartmentById(Long id);

    List<ApartmentResponseDTO> getAllApartments();

    Page<ApartmentResponseDTO> searchApartments(ApartmentFilterRequest filter);

    List<ApartmentResponseDTO> getApartmentsByBuildingId(Long buildingId);

    ApartmentResponseDTO updateApartment(Long id, UpdateApartmentRequest request);

    void deleteApartment(Long id);
}
