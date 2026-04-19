package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.AIClassificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIClassificationLogRepository extends JpaRepository<AIClassificationLog, Long> {

    List<AIClassificationLog> findByIssueId(Long issueId);

    List<AIClassificationLog> findByPredictedCategory(String predictedCategory);

    List<AIClassificationLog> findByPredictedPriority(String predictedPriority);
}