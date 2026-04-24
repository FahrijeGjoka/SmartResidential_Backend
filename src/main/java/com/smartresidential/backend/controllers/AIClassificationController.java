package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.aiClassificationLog.AIClassificationLogResponseDTO;
import com.smartresidential.backend.dto.aiClassificationLog.CreateAIClassificationLogRequest;
import com.smartresidential.backend.services.interfaces.AIClassificationLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AIClassificationController {

    private final AIClassificationLogService service;

    public AIClassificationController(AIClassificationLogService service) {
        this.service = service;
    }

    @PostMapping("/classify-issue")
    public AIClassificationLogResponseDTO classify(@RequestBody CreateAIClassificationLogRequest request) {
        return service.create(request);
    }

    @GetMapping("/logs")
    public List<AIClassificationLogResponseDTO> getAllLogs() {
        return service.getByIssue(0L); // workaround pa service change
    }

    @GetMapping("/logs/issue/{issueId}")
    public List<AIClassificationLogResponseDTO> getByIssue(@PathVariable Long issueId) {
        return service.getByIssue(issueId);
    }

    @GetMapping("/logs/category")
    public List<AIClassificationLogResponseDTO> getByCategory(@RequestParam String category) {
        return service.getByCategory(category);
    }

    @GetMapping("/logs/priority")
    public List<AIClassificationLogResponseDTO> getByPriority(@RequestParam String priority) {
        return service.getByPriority(priority);
    }

    @GetMapping("/logs/latest/{issueId}")
    public AIClassificationLogResponseDTO getLatest(@PathVariable Long issueId) {
        return service.getLatestByIssue(issueId);
    }
}