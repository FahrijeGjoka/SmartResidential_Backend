package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.auditLog.AuditLogResponseDTO;
import com.smartresidential.backend.services.interfaces.AuditLogService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditLogControllerTest {

    private final AuditLogService auditLogService = mock(AuditLogService.class);
    private final AuditLogController controller = new AuditLogController(auditLogService);

    @Test
    void getAllReturnsAllAuditLogsInsteadOfNullUserLogs() {
        AuditLogResponseDTO first = dto(1L, 10L, "CREATE", "ISSUE", 100L);
        AuditLogResponseDTO second = dto(2L, null, "SYSTEM", "JOB", 200L);

        when(auditLogService.getAll()).thenReturn(List.of(first, second));

        List<AuditLogResponseDTO> response = controller.getAll();

        assertThat(response).containsExactly(first, second);
        verify(auditLogService).getAll();
        verify(auditLogService, never()).getByUser(null);
    }

    private AuditLogResponseDTO dto(Long id, Long userId, String action, String entityType, Long entityId) {
        AuditLogResponseDTO dto = new AuditLogResponseDTO();
        dto.setId(id);
        dto.setUserId(userId);
        dto.setAction(action);
        dto.setEntityType(entityType);
        dto.setEntityId(entityId);
        dto.setCreatedAt("2026-05-23T12:00:00");
        return dto;
    }
}
