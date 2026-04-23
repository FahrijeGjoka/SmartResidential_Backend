package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.technicianProfile.CreateTechnicianProfileRequest;
import com.smartresidential.backend.dto.technicianProfile.TechnicianProfileResponseDTO;
import com.smartresidential.backend.dto.technicianProfile.UpdateTechnicianProfileRequest;
import com.smartresidential.backend.entities.TechnicianProfile;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.repositories.TechnicianProfileRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.TechnicianProfileService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnicianProfileServiceImpl implements TechnicianProfileService {

    private final TechnicianProfileRepository repository;
    private final UserRepository userRepository;

    public TechnicianProfileServiceImpl(TechnicianProfileRepository repository,
                                        UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public TechnicianProfileResponseDTO create(CreateTechnicianProfileRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        TechnicianProfile profile = new TechnicianProfile();
        profile.setUser(user);
        profile.setSpecialization(request.getSpecialization());
        profile.setIsAvailable(request.getIsAvailable());

        return mapToDTO(repository.save(profile));
    }

    @Override
    public TechnicianProfileResponseDTO getByUserId(Long userId) {
        return repository.findByUserId(userId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    @Override
    public List<TechnicianProfileResponseDTO> getAvailable() {
        return repository.findByIsAvailableTrue()
                .stream().map(this::mapToDTO).toList();
    }

    @Override
    public List<TechnicianProfileResponseDTO> getBySpecialization(String specialization) {
        return repository.findBySpecialization(specialization)
                .stream().map(this::mapToDTO).toList();
    }

    @Override
    public TechnicianProfileResponseDTO update(Long id, UpdateTechnicianProfileRequest request) {
        TechnicianProfile profile = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (request.getSpecialization() != null)
            profile.setSpecialization(request.getSpecialization());

        if (request.getIsAvailable() != null)
            profile.setIsAvailable(request.getIsAvailable());

        return mapToDTO(repository.save(profile));
    }

    @Override
    public void changeAvailability(Long id, Boolean isAvailable) {
        TechnicianProfile profile = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setIsAvailable(isAvailable);
        repository.save(profile);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private TechnicianProfileResponseDTO mapToDTO(TechnicianProfile p) {
        TechnicianProfileResponseDTO dto = new TechnicianProfileResponseDTO();
        dto.setId(p.getId());
        dto.setUserId(p.getUser().getId());
        dto.setSpecialization(p.getSpecialization());
        dto.setIsAvailable(p.getIsAvailable());
        return dto;
    }
}