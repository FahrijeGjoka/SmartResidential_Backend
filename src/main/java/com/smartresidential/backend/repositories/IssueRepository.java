package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findByStatus(String status);

    List<Issue> findByPriority(String priority);

    List<Issue> findByCategoryId(Long categoryId);

    List<Issue> findByApartmentId(Long apartmentId);

    List<Issue> findByCreatedById(Long userId);

    List<Issue> findByTitleContainingIgnoreCase(String title);
}