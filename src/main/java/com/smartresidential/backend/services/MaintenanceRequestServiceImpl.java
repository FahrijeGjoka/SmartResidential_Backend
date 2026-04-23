package com.smartresidential.backend.services;



import com.smartresidential.backend.dto.maintenanceRequest.CreateMaintenanceRequestRequest;
import com.smartresidential.backend.dto.maintenanceRequest.MaintenanceRequestResponseDTO;
import com.smartresidential.backend.entities.MaintenanceRequest;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.repositories.MaintenanceRequestRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.services.MaintenanceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MaintenanceRequestServiceImpl implements MaintenanceRequestService {

    @Autowired
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Override
    public MaintenanceRequestResponseDTO createMaintenanceRequest(CreateMaintenanceRequestRequest request) {
        // Validate issue
        Optional<Issue> issueOptional = issueRepository.findById(request.getIssueId());
        if (!issueOptional.isPresent()) {
            throw new IllegalArgumentException("Issue not found");
        }

        // Validate user
        Optional<User> userOptional = userRepository.findById(request.getRequestedById());
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found");
        }

        // Create MaintenanceRequest entity
        MaintenanceRequest maintenanceRequest = new MaintenanceRequest();
        maintenanceRequest.setIssue(issueOptional.get());
        maintenanceRequest.setRequestedBy(userOptional.get());
        maintenanceRequest.setRequestNote(request.getRequestNote());
        maintenanceRequest.setRequestedAt(LocalDateTime.now());

        // Save MaintenanceRequest entity
        MaintenanceRequest savedRequest = maintenanceRequestRepository.save(maintenanceRequest);

        // Return response DTO
        return convertToResponseDTO(savedRequest);
    }

    @Override
    public MaintenanceRequestResponseDTO getMaintenanceRequestById(Long id) {
        Optional<MaintenanceRequest> maintenanceRequestOptional = maintenanceRequestRepository.findById(id);
        if (!maintenanceRequestOptional.isPresent()) {
            throw new IllegalArgumentException("Maintenance request not found");
        }
        return convertToResponseDTO(maintenanceRequestOptional.get());
    }

    private MaintenanceRequestResponseDTO convertToResponseDTO(MaintenanceRequest maintenanceRequest) {
        MaintenanceRequestResponseDTO dto = new MaintenanceRequestResponseDTO();
        dto.setId(maintenanceRequest.getId());
        dto.setIssueId(maintenanceRequest.getIssue().getId());
        dto.setRequestedById(maintenanceRequest.getRequestedBy().getId());
        dto.setDescription(maintenanceRequest.getRequestNote());
        dto.setRequestedAt(maintenanceRequest.getRequestedAt());
        return dto;
    }
}