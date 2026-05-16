package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.aiClassificationLog.AIClassificationLogResponseDTO;
import com.smartresidential.backend.dto.aiClassificationLog.CreateAIClassificationLogRequest;
import com.smartresidential.backend.entities.*;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.repositories.*;
import com.smartresidential.backend.services.interfaces.AIClassificationLogService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AIClassificationLogServiceImpl implements AIClassificationLogService {

    private final AIClassificationLogRepository repository;
    private final IssueRepository issueRepository;
    private final OllamaService ollamaService;
    private final TechnicianProfileRepository technicianProfileRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;

    public AIClassificationLogServiceImpl(AIClassificationLogRepository repository,
                                          IssueRepository issueRepository,
                                          OllamaService ollamaService,
                                          TechnicianProfileRepository technicianProfileRepository,
                                          NotificationRepository notificationRepository,
                                          AuditLogRepository auditLogRepository) {
        this.repository = repository;
        this.issueRepository = issueRepository;
        this.ollamaService = ollamaService;
        this.technicianProfileRepository = technicianProfileRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public AIClassificationLogResponseDTO create(CreateAIClassificationLogRequest request) {

        Issue issue = issueRepository.findById(request.getIssueId())
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        // Thirr Ollama-n për klasifikim
        OllamaService.AIClassificationResult aiResult = ollamaService.classifyIssue(request.getRawInput());

        // Ruaj klasifikimin
        AIClassificationLog log = new AIClassificationLog();
        log.setIssue(issue);
        log.setRawInput(request.getRawInput());
        log.setPredictedCategory(aiResult.getPredictedCategory());
        log.setPredictedPriority(aiResult.getPredictedPriority());
        log.setConfidenceScore(aiResult.getConfidenceScore());
        AIClassificationLog savedLog = repository.save(log);

        // Gjej teknikun më të mirë
        TechnicianProfile bestTechnician = findBestTechnician(aiResult.getPredictedCategory());

        String technicianInfo = "Nuk u gjet teknik";
        if (bestTechnician != null) {
            technicianInfo = bestTechnician.getUser().getFirstName() + " " +
                    bestTechnician.getUser().getLastName() +
                    " (" + bestTechnician.getSpecialization() + ")";
        }

        // Dërgo njoftim
        if (bestTechnician != null) {
            Notification notification = new Notification();
            notification.setUser(bestTechnician.getUser());
            notification.setMessage("Detyrë e re: " + issue.getDescription() +
                    " | Kategoria: " + aiResult.getPredictedCategory() +
                    " | Prioriteti: " + aiResult.getPredictedPriority());
            notification.setType("NEW_ASSIGNMENT");
            notificationRepository.save(notification);
        }

        // Regjistro audit log
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(issue.getCreatedBy());
        auditLog.setAction("AI_CLASSIFICATION");
        auditLog.setEntityType("Issue");
        auditLog.setEntityId(issue.getId());
        auditLogRepository.save(auditLog);

        AIClassificationLogResponseDTO dto = mapToDTO(savedLog);
        dto.setSuggestedTechnician(technicianInfo);

        return dto;
    }

    private TechnicianProfile findBestTechnician(String category) {
        List<TechnicianProfile> available = technicianProfileRepository
                .findBySpecialization(category)
                .stream()
                .filter(TechnicianProfile::getIsAvailable)
                .toList();
        return !available.isEmpty() ? available.get(0) : null;
    }

    // Metodat e tjera mbeten të njëjta...
    @Override
    public List<AIClassificationLogResponseDTO> getByIssue(Long issueId) {
        return repository.findByIssueId(issueId).stream().map(this::mapToDTO).toList();
    }

    @Override
    public List<AIClassificationLogResponseDTO> getByCategory(String category) {
        return repository.findByPredictedCategory(category).stream().map(this::mapToDTO).toList();
    }

    @Override
    public List<AIClassificationLogResponseDTO> getByPriority(String priority) {
        return repository.findByPredictedPriority(priority).stream().map(this::mapToDTO).toList();
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
