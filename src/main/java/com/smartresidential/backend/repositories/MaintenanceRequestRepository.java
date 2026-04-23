package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.MaintenanceRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MaintenanceRequestRepository extends BaseRepository<MaintenanceRequest, Long> {

    Optional<MaintenanceRequest> findByIssue_Id(Long issueId);

    boolean existsByIssue_Id(Long issueId);

    List<MaintenanceRequest> findByRequestedBy_Id(Long requestedById);

    List<MaintenanceRequest> findByRequestedAt(LocalDateTime requestedAt);
}