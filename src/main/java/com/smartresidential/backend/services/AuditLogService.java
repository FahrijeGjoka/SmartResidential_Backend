package com.smartresidential.backend.services;

import com.smartresidential.backend.dto.auditLog.AuditLogResponseDTO;
import com.smartresidential.backend.dto.auditLog.CreateAuditLogRequest;

import java.util.List;

public interface AuditLogService {

    void log(CreateAuditLogRequest request);

    List<AuditLogResponseDTO> getByUser(Long userId);

    List<AuditLogResponseDTO> getByEntity(Long entityId);
}