package com.smartresidential.backend.repositories;


import com.smartresidential.backend.entities.WorkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {

    List<WorkLog> findByCreatedAt(LocalDate createdAt);


    List<WorkLog> findByTechnicianId(Long technicianId);


    List<WorkLog> findByIssueId(Long issueId);
}
