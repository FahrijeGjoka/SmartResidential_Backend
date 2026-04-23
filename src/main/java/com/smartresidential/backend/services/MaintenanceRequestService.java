package com.smartresidential.backend.services;

import com.smartresidential.backend.dto.maintenanceRequest.CreateMaintenanceRequestRequest;
import com.smartresidential.backend.dto.maintenanceRequest.MaintenanceRequestResponseDTO;

public interface MaintenanceRequestService {

    MaintenanceRequestResponseDTO createMaintenanceRequest(CreateMaintenanceRequestRequest request);

    MaintenanceRequestResponseDTO getMaintenanceRequestById(Long id);
}