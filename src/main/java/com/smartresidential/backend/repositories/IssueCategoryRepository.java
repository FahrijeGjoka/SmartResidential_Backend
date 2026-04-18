package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.IssueCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IssueCategoryRepository extends JpaRepository<IssueCategory, Long> {

    Optional<IssueCategory> findByName(String name);

    boolean existsByName(String name);
}