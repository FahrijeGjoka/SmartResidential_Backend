package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.auditLog.AuditLogFilterRequest;
import com.smartresidential.backend.dto.auditLog.AuditLogResponseDTO;
import com.smartresidential.backend.dto.auditLog.CreateAuditLogRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AuditLogService {

    void log(CreateAuditLogRequest request);

    void logCurrentUser(String action, String entityType, Long entityId);

    List<AuditLogResponseDTO> getAll();

    Page<AuditLogResponseDTO> search(AuditLogFilterRequest filter);

    List<AuditLogResponseDTO> getByUser(Long userId);

    List<AuditLogResponseDTO> getByEntity(Long entityId);
}
