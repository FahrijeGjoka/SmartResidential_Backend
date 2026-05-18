package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.ai.IssueCategoryMatchRequest;
import com.smartresidential.backend.dto.ai.IssueCategoryMatchResponse;
import com.smartresidential.backend.dto.aiClassificationLog.AIClassificationLogResponseDTO;
import com.smartresidential.backend.dto.aiClassificationLog.CreateAIClassificationLogRequest;
import com.smartresidential.backend.services.impl.OllamaService;
import com.smartresidential.backend.services.interfaces.AIClassificationLogService;
import com.smartresidential.backend.services.interfaces.IssueCategoryMatcherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
public class AIClassificationController {

    private final AIClassificationLogService service;
    private final OllamaService ollamaService;
    private final IssueCategoryMatcherService issueCategoryMatcherService;

    public AIClassificationController(
            AIClassificationLogService service,
            OllamaService ollamaService,
            IssueCategoryMatcherService issueCategoryMatcherService
    ) {
        this.service = service;
        this.ollamaService = ollamaService;
        this.issueCategoryMatcherService = issueCategoryMatcherService;
    }

    @PostMapping("/classify-issue")
    public AIClassificationLogResponseDTO classify(@RequestBody CreateAIClassificationLogRequest request) {
        return service.create(request);
    }

    @PostMapping("/issue-category-match")
    public IssueCategoryMatchResponse matchIssueCategory(@RequestBody IssueCategoryMatchRequest request) {
        return issueCategoryMatcherService.matchCategoryForResponse(
                request.getTitle(),
                request.getDescription()
        );
    }

    @PostMapping("/test-ollama")
    public ResponseEntity<?> testOllama(@RequestBody Map<String, String> request) {
        String text = request.get("rawInput");
        if (text == null || text.isEmpty()) {
            return ResponseEntity.badRequest().body("rawInput is required");
        }
        OllamaService.AIClassificationResult result = ollamaService.classifyIssue(text);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/logs")
    public List<AIClassificationLogResponseDTO> getAllLogs() {
        return service.getByIssue(0L);
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
