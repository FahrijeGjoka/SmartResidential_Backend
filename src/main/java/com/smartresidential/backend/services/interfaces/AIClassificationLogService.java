package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.aiClassificationLog.AIClassificationLogResponseDTO;
import com.smartresidential.backend.dto.aiClassificationLog.CreateAIClassificationLogRequest;

import java.util.List;

public interface AIClassificationLogService {

    AIClassificationLogResponseDTO create(CreateAIClassificationLogRequest request);

    List<AIClassificationLogResponseDTO> getByIssue(Long issueId);

    List<AIClassificationLogResponseDTO> getByCategory(String category);

    List<AIClassificationLogResponseDTO> getByPriority(String priority);

    AIClassificationLogResponseDTO getLatestByIssue(Long issueId);
}