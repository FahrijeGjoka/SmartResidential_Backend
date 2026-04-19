package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {
    List<MaintenanceRequest> findByScheduledDate(LocalDate scheduledDate);
    List<MaintenanceRequest> findByTechnicianId(Long technicianId);
}