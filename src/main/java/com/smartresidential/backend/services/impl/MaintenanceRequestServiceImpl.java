package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.maintenanceRequest.CreateMaintenanceRequestRequest;
import com.smartresidential.backend.dto.maintenanceRequest.MaintenanceRequestResponseDTO;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.MaintenanceRequest;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.MaintenanceRequestRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.MaintenanceRequestService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceRequestServiceImpl implements MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;

    public MaintenanceRequestServiceImpl(
            MaintenanceRequestRepository maintenanceRequestRepository,
            UserRepository userRepository,
            IssueRepository issueRepository
    ) {
        this.maintenanceRequestRepository = maintenanceRequestRepository;
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
    }

    @Override
    public MaintenanceRequestResponseDTO createMaintenanceRequest(CreateMaintenanceRequestRequest request) {
        Issue issue = issueRepository.findById(request.getIssueId())
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        User user = userRepository.findById(request.getRequestedById())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (maintenanceRequestRepository.existsByIssue_Id(request.getIssueId())) {
            throw new IllegalArgumentException("Maintenance request already exists for this issue");
        }

        MaintenanceRequest maintenanceRequest = new MaintenanceRequest();
        maintenanceRequest.setIssue(issue);
        maintenanceRequest.setRequestedBy(user);
        maintenanceRequest.setDescription(request.getDescription());

        MaintenanceRequest savedRequest = maintenanceRequestRepository.save(maintenanceRequest);

        return convertToResponseDTO(savedRequest);
    }

    @Override
    public MaintenanceRequestResponseDTO getMaintenanceRequestById(Long id) {
        MaintenanceRequest maintenanceRequest = maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance request not found"));

        return convertToResponseDTO(maintenanceRequest);
    }

    @Override
    public List<MaintenanceRequestResponseDTO> getAllMaintenanceRequests() {
        return maintenanceRequestRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Override
    public boolean existsByIssueId(Long issueId) {
        return maintenanceRequestRepository.existsByIssue_Id(issueId);
    }

    private MaintenanceRequestResponseDTO convertToResponseDTO(MaintenanceRequest maintenanceRequest) {
        MaintenanceRequestResponseDTO dto = new MaintenanceRequestResponseDTO();
        dto.setId(maintenanceRequest.getId());
        dto.setIssueId(maintenanceRequest.getIssue().getId());
        dto.setRequestedById(maintenanceRequest.getRequestedBy().getId());
        dto.setDescription(maintenanceRequest.getDescription());
        dto.setRequestedAt(maintenanceRequest.getRequestedAt());
        return dto;
    }
}