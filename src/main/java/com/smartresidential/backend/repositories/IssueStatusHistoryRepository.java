package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.IssueStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueStatusHistoryRepository extends JpaRepository<IssueStatusHistory, Long> {

    List<IssueStatusHistory> findByIssueId(Long issueId);

    List<IssueStatusHistory> findByChangedById(Long userId);

    List<IssueStatusHistory> findByIssueIdOrderByChangedAtDesc(Long issueId);
}