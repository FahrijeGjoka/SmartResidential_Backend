package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.auditLog.AuditLogResponseDTO;
import com.smartresidential.backend.dto.auditLog.CreateAuditLogRequest;
import com.smartresidential.backend.entities.AuditLog;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.repositories.AuditLogRepository;
import com.smartresidential.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository repository;

    @Mock
    private UserRepository userRepository;

    private AuditLogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuditLogServiceImpl(repository, userRepository);
    }

    @Test
    void logCreatesAuditLogWithExistingUser() {

        CreateAuditLogRequest request = new CreateAuditLogRequest();
        request.setUserId(1L);
        request.setAction("CREATE");
        request.setEntityType("ISSUE");
        request.setEntityId(100L);

        User user = user(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        service.log(request);

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(repository).save(captor.capture());

        AuditLog saved = captor.getValue();

        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getAction()).isEqualTo("CREATE");
        assertThat(saved.getEntityType()).isEqualTo("ISSUE");
        assertThat(saved.getEntityId()).isEqualTo(100L);
    }

    @Test
    void logCreatesAuditLogWithoutUserWhenUserDoesNotExist() {

        CreateAuditLogRequest request = new CreateAuditLogRequest();
        request.setUserId(99L);
        request.setAction("DELETE");
        request.setEntityType("BUILDING");
        request.setEntityId(50L);

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        service.log(request);

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(repository).save(captor.capture());

        AuditLog saved = captor.getValue();

        assertThat(saved.getUser()).isNull();
        assertThat(saved.getAction()).isEqualTo("DELETE");
        assertThat(saved.getEntityType()).isEqualTo("BUILDING");
        assertThat(saved.getEntityId()).isEqualTo(50L);
    }

    @Test
    void logDoesNotCallUserRepositoryWhenUserIdIsNull() {

        CreateAuditLogRequest request = new CreateAuditLogRequest();
        request.setAction("UPDATE");
        request.setEntityType("APARTMENT");
        request.setEntityId(25L);

        service.log(request);

        verify(userRepository, never()).findById(any());

        verify(repository).save(any(AuditLog.class));
    }

    @Test
    void getByUserReturnsMappedDtos() {

        User user = user(1L);

        AuditLog log = auditLog(
                10L,
                user,
                "CREATE",
                "ISSUE",
                100L
        );

        when(repository.findByUserId(1L))
                .thenReturn(List.of(log));

        List<AuditLogResponseDTO> result =
                service.getByUser(1L);

        assertThat(result).hasSize(1);

        AuditLogResponseDTO dto = result.get(0);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getAction()).isEqualTo("CREATE");
        assertThat(dto.getEntityType()).isEqualTo("ISSUE");
        assertThat(dto.getEntityId()).isEqualTo(100L);
        assertThat(dto.getCreatedAt()).isNotNull();
    }

    @Test
    void getByEntityReturnsMappedDtos() {

        User user = user(2L);

        AuditLog log = auditLog(
                20L,
                user,
                "UPDATE",
                "BUILDING",
                200L
        );

        when(repository.findByEntityId(200L))
                .thenReturn(List.of(log));

        List<AuditLogResponseDTO> result =
                service.getByEntity(200L);

        assertThat(result).hasSize(1);

        AuditLogResponseDTO dto = result.get(0);

        assertThat(dto.getId()).isEqualTo(20L);
        assertThat(dto.getUserId()).isEqualTo(2L);
        assertThat(dto.getAction()).isEqualTo("UPDATE");
        assertThat(dto.getEntityType()).isEqualTo("BUILDING");
        assertThat(dto.getEntityId()).isEqualTo(200L);
        assertThat(dto.getCreatedAt()).isNotNull();
    }

    @Test
    void getByUserReturnsEmptyListWhenNoLogsExist() {

        when(repository.findByUserId(1L))
                .thenReturn(List.of());

        List<AuditLogResponseDTO> result =
                service.getByUser(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getByEntityReturnsEmptyListWhenNoLogsExist() {

        when(repository.findByEntityId(500L))
                .thenReturn(List.of());

        List<AuditLogResponseDTO> result =
                service.getByEntity(500L);

        assertThat(result).isEmpty();
    }

    private User user(Long id) {

        User user = new User();
        user.setId(id);
        user.setRoleId(4L);
        user.setEmail("user" + id + "@example.com");
        user.setPasswordHash("password");
        user.setIsActive(true);

        return user;
    }

    private AuditLog auditLog(
            Long id,
            User user,
            String action,
            String entityType,
            Long entityId
    ) {

        AuditLog log = new AuditLog();

        log.setId(id);
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setCreatedAt(LocalDateTime.now());

        return log;
    }
}