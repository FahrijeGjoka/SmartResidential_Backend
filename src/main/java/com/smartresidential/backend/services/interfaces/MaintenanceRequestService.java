package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.maintenanceRequest.CreateMaintenanceRequestRequest;
import com.smartresidential.backend.dto.maintenanceRequest.MaintenanceRequestResponseDTO;

import java.util.List;

public interface MaintenanceRequestService {

    MaintenanceRequestResponseDTO createMaintenanceRequest(CreateMaintenanceRequestRequest request);

    MaintenanceRequestResponseDTO getMaintenanceRequestById(Long id);

    List<MaintenanceRequestResponseDTO> getAllMaintenanceRequests();

    boolean existsByIssueId(Long issueId);
}