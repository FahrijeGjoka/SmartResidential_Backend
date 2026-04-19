package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.IssueStatusHistory;

import java.util.List;

public interface IssueStatusHistoryRepository extends BaseRepository<IssueStatusHistory, Long> {

    List<IssueStatusHistory> findByIssueId(Long issueId);

    List<IssueStatusHistory> findByChangedById(Long userId);

    List<IssueStatusHistory> findByIssueIdOrderByChangedAtDesc(Long issueId);
}