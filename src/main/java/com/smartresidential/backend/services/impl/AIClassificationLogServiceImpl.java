package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.aiClassificationLog.AIClassificationLogResponseDTO;
import com.smartresidential.backend.dto.aiClassificationLog.CreateAIClassificationLogRequest;
import com.smartresidential.backend.entities.AIClassificationLog;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.repositories.AIClassificationLogRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.services.interfaces.AIClassificationLogService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AIClassificationLogServiceImpl implements AIClassificationLogService {

    private final AIClassificationLogRepository repository;
    private final IssueRepository issueRepository;

    public AIClassificationLogServiceImpl(AIClassificationLogRepository repository,
                                          IssueRepository issueRepository) {
        this.repository = repository;
        this.issueRepository = issueRepository;
    }

    @Override
    public AIClassificationLogResponseDTO create(CreateAIClassificationLogRequest request) {
        Issue issue = issueRepository.findById(request.getIssueId())
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        AIClassificationLog log = new AIClassificationLog();
        log.setIssue(issue);
        log.setRawInput(request.getRawInput());
        log.setPredictedCategory(request.getPredictedCategory());
        log.setPredictedPriority(request.getPredictedPriority());
        log.setConfidenceScore(request.getConfidenceScore());

        return mapToDTO(repository.save(log));
    }

    @Override
    public List<AIClassificationLogResponseDTO> getByIssue(Long issueId) {
        return repository.findByIssueId(issueId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<AIClassificationLogResponseDTO> getByCategory(String category) {
        return repository.findByPredictedCategory(category)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<AIClassificationLogResponseDTO> getByPriority(String priority) {
        return repository.findByPredictedPriority(priority)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public AIClassificationLogResponseDTO getLatestByIssue(Long issueId) {
        return repository.findByIssueId(issueId)
                .stream()
                .max(Comparator.comparing(AIClassificationLog::getCreatedAt))
                .map(this::mapToDTO)
                .orElse(null);
    }

    private AIClassificationLogResponseDTO mapToDTO(AIClassificationLog log) {
        AIClassificationLogResponseDTO dto = new AIClassificationLogResponseDTO();
        dto.setId(log.getId());
        dto.setIssueId(log.getIssue().getId());
        dto.setRawInput(log.getRawInput());
        dto.setPredictedCategory(log.getPredictedCategory());
        dto.setPredictedPriority(log.getPredictedPriority());
        dto.setConfidenceScore(log.getConfidenceScore());
        dto.setCreatedAt(log.getCreatedAt().toString());
        return dto;
    }
}
