package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.AuditLog;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends BaseRepository<AuditLog, Long> {

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findAllByOrderByCreatedAtDescIdDesc();

    List<AuditLog> findByEntityType(String entityType);

    List<AuditLog> findByEntityId(Long entityId);

    List<AuditLog> findByAction(String action);

    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}
