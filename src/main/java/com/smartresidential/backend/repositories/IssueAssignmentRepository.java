package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.IssueAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueAssignmentRepository extends JpaRepository<IssueAssignment, Long> {

    List<IssueAssignment> findByIssueId(Long issueId);

    List<IssueAssignment> findByTechnicianId(Long technicianId);
}