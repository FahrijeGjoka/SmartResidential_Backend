package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.Issue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IssueRepository extends BaseRepository<Issue, Long> {

    List<Issue> findByStatus(String status);

    List<Issue> findByPriority(String priority);

    List<Issue> findByCategoryId(Long categoryId);

    List<Issue> findByApartmentId(Long apartmentId);

    List<Issue> findByCreatedById(Long userId);

    long countByCreatedByIdAndStatusIn(Long userId, List<String> statuses);

    Optional<Issue> findTopByCreatedByIdOrderByUpdatedAtDescIdDesc(Long userId);

    List<Issue> findByTitleContainingIgnoreCase(String title);

    List<Issue> findByStatusInAndCreatedAtBefore(List<String> statuses, LocalDateTime dateTime);
}
