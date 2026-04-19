package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.IssueCategory;

import java.util.Optional;

public interface IssueCategoryRepository extends BaseRepository<IssueCategory, Long>  {

    Optional<IssueCategory> findByName(String name);

    boolean existsByName(String name);
}