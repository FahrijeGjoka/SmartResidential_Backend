package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.residentProfile.CreateResidentProfileRequest;
import com.smartresidential.backend.dto.residentProfile.ResidentProfileResponseDTO;
import com.smartresidential.backend.dto.residentProfile.UpdateResidentProfileRequest;
import com.smartresidential.backend.dto.residentprofile.ResidentProfileFilterRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ResidentProfileService {

    ResidentProfileResponseDTO createResidentProfile(CreateResidentProfileRequest request);

    ResidentProfileResponseDTO getResidentProfileById(Long id);

    List<ResidentProfileResponseDTO> getAllResidentProfiles();

    Page<ResidentProfileResponseDTO> searchResidentProfiles(ResidentProfileFilterRequest filter);

    List<ResidentProfileResponseDTO> getResidentProfilesByBuildingId(Long buildingId);

    ResidentProfileResponseDTO updateResidentProfile(Long id, UpdateResidentProfileRequest request);

    void deleteResidentProfile(Long id);
}
