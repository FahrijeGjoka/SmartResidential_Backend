package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.aiClassificationLog.AIClassificationLogResponseDTO;
import com.smartresidential.backend.dto.aiClassificationLog.CreateAIClassificationLogRequest;
import com.smartresidential.backend.services.impl.OllamaService;
import com.smartresidential.backend.services.interfaces.AIClassificationLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIClassificationController {

    private final AIClassificationLogService service;
    private final OllamaService ollamaService;  // 👈 SHTO KËTË

    // 👈 PËRDITËSO KONSTRUKTORIN
    public AIClassificationController(AIClassificationLogService service, OllamaService ollamaService) {
        this.service = service;
        this.ollamaService = ollamaService;
    }

    @PostMapping("/classify-issue")
    public AIClassificationLogResponseDTO classify(@RequestBody CreateAIClassificationLogRequest request) {
        return service.create(request);
    }

    // 👈 SHTO KËTË METODË
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