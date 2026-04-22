package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    Optional<MaintenanceRequest> findByIssue_Id(Long issueId);

    List<MaintenanceRequest> findByRequestedBy_Id(Long requestedById);

    List<MaintenanceRequest> findByRequestedAt(java.time.LocalDateTime requestedAt);
}