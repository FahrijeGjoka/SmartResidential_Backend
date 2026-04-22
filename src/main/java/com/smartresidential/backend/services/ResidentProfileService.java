package com.smartresidential.backend.services;

import com.smartresidential.backend.dto.residentProfile.CreateResidentProfileRequest;
import com.smartresidential.backend.dto.residentProfile.ResidentProfileResponseDTO;
import com.smartresidential.backend.dto.residentProfile.UpdateResidentProfileRequest;

import java.util.List;

public interface ResidentProfileService {

    ResidentProfileResponseDTO createResidentProfile(CreateResidentProfileRequest request);

    ResidentProfileResponseDTO getResidentProfileById(Long id);

    List<ResidentProfileResponseDTO> getAllResidentProfiles();

    List<ResidentProfileResponseDTO> getResidentProfilesByBuildingId(Long buildingId);

    ResidentProfileResponseDTO updateResidentProfile(Long id, UpdateResidentProfileRequest request);

    void deleteResidentProfile(Long id);
}