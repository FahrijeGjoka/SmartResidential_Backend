package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.auditLog.AuditLogResponseDTO;
import com.smartresidential.backend.dto.auditLog.CreateAuditLogRequest;
import com.smartresidential.backend.entities.AuditLog;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.repositories.AuditLogRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.AuditLogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository repository;
    private final UserRepository userRepository;

    public AuditLogServiceImpl(AuditLogRepository repository,
                               UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public void log(CreateAuditLogRequest request) {
        AuditLog log = new AuditLog();

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElse(null);
            log.setUser(user);
        }

        log.setAction(request.getAction());
        log.setEntityType(request.getEntityType());
        log.setEntityId(request.getEntityId());

        repository.save(log);
    }

    @Override
    public List<AuditLogResponseDTO> getByUser(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<AuditLogResponseDTO> getByEntity(Long entityId) {
        return repository.findByEntityId(entityId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private AuditLogResponseDTO mapToDTO(AuditLog log) {
        AuditLogResponseDTO dto = new AuditLogResponseDTO();
        dto.setId(log.getId());
        dto.setUserId(log.getUser() != null ? log.getUser().getId() : null);
        dto.setAction(log.getAction());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setCreatedAt(log.getCreatedAt().toString());
        return dto;
    }
}