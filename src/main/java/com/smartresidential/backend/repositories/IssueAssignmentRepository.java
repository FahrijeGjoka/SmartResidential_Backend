package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.IssueAssignment;

import java.util.List;

public interface IssueAssignmentRepository extends BaseRepository<IssueAssignment, Long>  {

    List<IssueAssignment> findByIssueId(Long issueId);

    List<IssueAssignment> findByTechnicianId(Long technicianId);
}