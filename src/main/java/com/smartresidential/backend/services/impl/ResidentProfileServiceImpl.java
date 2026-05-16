package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.residentProfile.CreateResidentProfileRequest;
import com.smartresidential.backend.dto.residentProfile.ResidentProfileResponseDTO;
import com.smartresidential.backend.dto.residentProfile.UpdateResidentProfileRequest;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.ResidentProfile;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.ConflictException;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.repositories.ApartmentRepository;
import com.smartresidential.backend.repositories.ResidentProfileRepository;
import com.smartresidential.backend.repositories.RoleRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.ResidentProfileService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResidentProfileServiceImpl implements ResidentProfileService {

    private static final String ROLE_RESIDENT = "ROLE_RESIDENT";
    private static final String DUPLICATE_RESIDENT_PROFILE_MESSAGE = "This user is already linked as a resident.";

    private final ResidentProfileRepository residentProfileRepository;
    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final RoleRepository roleRepository;

    public ResidentProfileServiceImpl(ResidentProfileRepository residentProfileRepository,
                                      UserRepository userRepository,
                                      ApartmentRepository apartmentRepository,
                                      RoleRepository roleRepository) {
        this.residentProfileRepository = residentProfileRepository;
        this.userRepository = userRepository;
        this.apartmentRepository = apartmentRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public ResidentProfileResponseDTO createResidentProfile(CreateResidentProfileRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        validateResidentUser(user);
        ensureUserHasNoResidentProfile(user.getId(), null);

        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + request.getApartmentId()));

        ResidentProfile residentProfile = new ResidentProfile();
        residentProfile.setUser(user);
        residentProfile.setApartment(apartment);
        residentProfile.setMovedInAt(request.getMovedInAt());

        ResidentProfile savedResidentProfile = saveResidentProfile(residentProfile);
        return mapToDTO(savedResidentProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public ResidentProfileResponseDTO getResidentProfileById(Long id) {
        ResidentProfile residentProfile = residentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ResidentProfile not found with id: " + id));

        return mapToDTO(residentProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResidentProfileResponseDTO> getAllResidentProfiles() {
        return residentProfileRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResidentProfileResponseDTO> getResidentProfilesByBuildingId(Long buildingId) {
        return residentProfileRepository.findByApartmentBuildingId(buildingId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResidentProfileResponseDTO updateResidentProfile(Long id, UpdateResidentProfileRequest request) {
        ResidentProfile residentProfile = residentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ResidentProfile not found with id: " + id));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        validateResidentUser(user);
        ensureUserHasNoResidentProfile(user.getId(), residentProfile.getId());

        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + request.getApartmentId()));

        residentProfile.setUser(user);
        residentProfile.setApartment(apartment);
        residentProfile.setMovedInAt(request.getMovedInAt());

        ResidentProfile updatedResidentProfile = saveResidentProfile(residentProfile);
        return mapToDTO(updatedResidentProfile);
    }

    @Override
    public void deleteResidentProfile(Long id) {
        ResidentProfile residentProfile = residentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ResidentProfile not found with id: " + id));

        residentProfileRepository.delete(residentProfile);
    }

    private void validateResidentUser(User user) {
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("Resident user must be active.");
        }

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Resident user role not found."));

        if (!ROLE_RESIDENT.equals(role.getName())) {
            throw new IllegalArgumentException("Only users with ROLE_RESIDENT can be linked as residents.");
        }
    }

    private void ensureUserHasNoResidentProfile(Long userId, Long currentResidentProfileId) {
        if (currentResidentProfileId == null) {
            if (residentProfileRepository.existsByUserId(userId)) {
                throwDuplicateResidentProfileException();
            }
            return;
        }

        if (residentProfileRepository.existsByUserIdAndIdNot(userId, currentResidentProfileId)) {
            throwDuplicateResidentProfileException();
        }
    }

    private ResidentProfile saveResidentProfile(ResidentProfile residentProfile) {
        try {
            return residentProfileRepository.save(residentProfile);
        } catch (DataIntegrityViolationException ex) {
            throwDuplicateResidentProfileException();
            throw ex;
        }
    }

    private void throwDuplicateResidentProfileException() {
        throw new ConflictException(DUPLICATE_RESIDENT_PROFILE_MESSAGE);
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
