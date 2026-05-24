package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.technicianProfile.CreateTechnicianProfileRequest;
import com.smartresidential.backend.dto.technicianProfile.TechnicianProfileFilterRequest;
import com.smartresidential.backend.dto.technicianProfile.TechnicianProfileResponseDTO;
import com.smartresidential.backend.dto.technicianProfile.UpdateTechnicianProfileRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TechnicianProfileService {

    TechnicianProfileResponseDTO create(CreateTechnicianProfileRequest request);

    TechnicianProfileResponseDTO getById(Long id);

    TechnicianProfileResponseDTO getByUserId(Long userId);

    List<TechnicianProfileResponseDTO> getAll();

    Page<TechnicianProfileResponseDTO> search(TechnicianProfileFilterRequest filter);

    List<TechnicianProfileResponseDTO> getAvailable();

    List<TechnicianProfileResponseDTO> getBySpecialization(String specialization);

    TechnicianProfileResponseDTO update(Long id, UpdateTechnicianProfileRequest request);

    void changeAvailability(Long id, Boolean isAvailable);

    void delete(Long id);
}
