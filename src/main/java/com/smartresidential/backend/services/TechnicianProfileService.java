package com.smartresidential.backend.services;

import com.smartresidential.backend.dto.technicianProfile.CreateTechnicianProfileRequest;
import com.smartresidential.backend.dto.technicianProfile.TechnicianProfileResponseDTO;
import com.smartresidential.backend.dto.technicianProfile.UpdateTechnicianProfileRequest;

import java.util.List;

public interface TechnicianProfileService {

    TechnicianProfileResponseDTO create(CreateTechnicianProfileRequest request);

    TechnicianProfileResponseDTO getByUserId(Long userId);

    List<TechnicianProfileResponseDTO> getAvailable();

    List<TechnicianProfileResponseDTO> getBySpecialization(String specialization);

    TechnicianProfileResponseDTO update(Long id, UpdateTechnicianProfileRequest request);

    void changeAvailability(Long id, Boolean isAvailable);

    void delete(Long id);
}