package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.IssueAssignment;

import java.util.List;
import java.util.Optional;

public interface IssueAssignmentRepository extends BaseRepository<IssueAssignment, Long>  {

    List<IssueAssignment> findByIssueId(Long issueId);

    boolean existsByIssueId(Long issueId);

    Optional<IssueAssignment> findTopByIssueIdOrderByAssignedAtDescIdDesc(Long issueId);

    List<IssueAssignment> findByTechnicianId(Long technicianId);
}
