package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.Issue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IssueRepository extends BaseRepository<Issue, Long> {

    List<Issue> findByStatus(String status);

    List<Issue> findByArchivedFalse();

    List<Issue> findByStatusAndArchivedFalse(String status);

    List<Issue> findByPriority(String priority);

    List<Issue> findByPriorityAndArchivedFalse(String priority);

    List<Issue> findByCategoryId(Long categoryId);

    List<Issue> findByCategoryIdAndArchivedFalse(Long categoryId);

    List<Issue> findByApartmentId(Long apartmentId);

    List<Issue> findByApartmentIdAndArchivedFalse(Long apartmentId);

    List<Issue> findByCreatedById(Long userId);

    List<Issue> findByCreatedByIdAndArchivedFalse(Long userId);

    long countByCreatedByIdAndStatusInAndArchivedFalse(Long userId, List<String> statuses);

    Optional<Issue> findTopByCreatedByIdAndArchivedFalseOrderByUpdatedAtDescIdDesc(Long userId);

    List<Issue> findByTitleContainingIgnoreCase(String title);

    List<Issue> findByTitleContainingIgnoreCaseAndArchivedFalse(String title);

    List<Issue> findByStatusInAndCreatedAtBeforeAndArchivedFalse(List<String> statuses, LocalDateTime dateTime);
}
