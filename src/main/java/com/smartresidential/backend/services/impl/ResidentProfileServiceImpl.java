package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.residentProfile.CreateResidentProfileRequest;
import com.smartresidential.backend.dto.residentProfile.ResidentProfileResponseDTO;
import com.smartresidential.backend.dto.residentProfile.UpdateResidentProfileRequest;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.ResidentProfile;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.repositories.ApartmentRepository;
import com.smartresidential.backend.repositories.ResidentProfileRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.ResidentProfileService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResidentProfileServiceImpl implements ResidentProfileService {

    private final ResidentProfileRepository residentProfileRepository;
    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;

    public ResidentProfileServiceImpl(ResidentProfileRepository residentProfileRepository,
                                      UserRepository userRepository,
                                      ApartmentRepository apartmentRepository) {
        this.residentProfileRepository = residentProfileRepository;
        this.userRepository = userRepository;
        this.apartmentRepository = apartmentRepository;
    }

    @Override
    public ResidentProfileResponseDTO createResidentProfile(CreateResidentProfileRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new RuntimeException("Apartment not found with id: " + request.getApartmentId()));

        ResidentProfile residentProfile = new ResidentProfile();
        residentProfile.setUser(user);
        residentProfile.setApartment(apartment);
        residentProfile.setMovedInAt(request.getMovedInAt());

        ResidentProfile savedResidentProfile = residentProfileRepository.save(residentProfile);
        return mapToDTO(savedResidentProfile);
    }

    @Override
    public ResidentProfileResponseDTO getResidentProfileById(Long id) {
        ResidentProfile residentProfile = residentProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ResidentProfile not found with id: " + id));

        return mapToDTO(residentProfile);
    }

    @Override
    public List<ResidentProfileResponseDTO> getAllResidentProfiles() {
        return residentProfileRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResidentProfileResponseDTO> getResidentProfilesByBuildingId(Long buildingId) {
        return residentProfileRepository.findByApartmentBuildingId(buildingId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResidentProfileResponseDTO updateResidentProfile(Long id, UpdateResidentProfileRequest request) {
        ResidentProfile residentProfile = residentProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ResidentProfile not found with id: " + id));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new RuntimeException("Apartment not found with id: " + request.getApartmentId()));

        residentProfile.setUser(user);
        residentProfile.setApartment(apartment);
        residentProfile.setMovedInAt(request.getMovedInAt());

        ResidentProfile updatedResidentProfile = residentProfileRepository.save(residentProfile);
        return mapToDTO(updatedResidentProfile);
    }

    @Override
    public void deleteResidentProfile(Long id) {
        ResidentProfile residentProfile = residentProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ResidentProfile not found with id: " + id));

        residentProfileRepository.delete(residentProfile);
    }

    private ResidentProfileResponseDTO mapToDTO(ResidentProfile residentProfile) {
        return new ResidentProfileResponseDTO(
                residentProfile.getId(),
                residentProfile.getUser().getId(),
                residentProfile.getApartment().getId(),
                residentProfile.getMovedInAt()
        );
    }
}