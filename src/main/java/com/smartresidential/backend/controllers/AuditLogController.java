package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.auditLog.AuditLogResponseDTO;
import com.smartresidential.backend.dto.auditLog.CreateAuditLogRequest;
import com.smartresidential.backend.services.interfaces.AuditLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService service;

    public AuditLogController(AuditLogService service) {
        this.service = service;
    }

    @PostMapping
    public void log(@RequestBody CreateAuditLogRequest request) {
        service.log(request);
    }

    @GetMapping
    public List<AuditLogResponseDTO> getAll() {
        return service.getByUser(null);
    }

    @GetMapping("/user/{userId}")
    public List<AuditLogResponseDTO> getByUser(@PathVariable Long userId) {
        return service.getByUser(userId);
    }

    @GetMapping("/entity/{entityId}")
    public List<AuditLogResponseDTO> getByEntity(@PathVariable Long entityId) {
        return service.getByEntity(entityId);
    }
}