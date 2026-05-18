package com.smartresidential.backend.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Service
public class OllamaService {

    private static final Logger LOGGER = Logger.getLogger(OllamaService.class.getName());

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String ollamaApiUrl;
    private final String ollamaModelName;

    public OllamaService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${ollama.api.url:${OLLAMA_BASE_URL:http://localhost:11434}}") String ollamaApiUrl,
            @Value("${ollama.model.name:${OLLAMA_MODEL:llama3.1}}") String ollamaModelName,
            @Value("${ollama.connect-timeout-ms:${OLLAMA_CONNECT_TIMEOUT_MS:3000}}") int connectTimeoutMs,
            @Value("${ollama.read-timeout-ms:${OLLAMA_READ_TIMEOUT_MS:8000}}") int readTimeoutMs
    ) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .readTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
        this.objectMapper = new ObjectMapper();
        this.ollamaApiUrl = ollamaApiUrl;
        this.ollamaModelName = ollamaModelName;
    }

    public AIClassificationResult classifyIssue(String issueDescription) {
        String prompt = buildClassificationPrompt(issueDescription);
        String aiResponse = sendToOllama(prompt);
        return parseAIResponse(aiResponse);
    }

    public IssueCategoryResult classifyIssueCategory(
            String title,
            String description,
            List<String> categoryCandidates
    ) {
        String prompt = buildCategoryPrompt(title, description, categoryCandidates);
        String aiResponse = sendToOllama(prompt);
        return parseCategoryResponse(aiResponse);
    }

    private String buildClassificationPrompt(String userInput) {
        return """
            You are an intelligent assistant for residential building management.
            Your task is to classify reported issues from residents.
            
            ISSUE DESCRIPTION: %s
            
            Analyze the description and return ONLY a valid JSON object.
            The JSON must have exactly this format:
            {
                "predictedCategory": "CATEGORY",
                "predictedPriority": "PRIORITY",
                "confidenceScore": 0.95
            }
            
            Possible CATEGORIES: PLUMBING, ELECTRICAL, HVAC, STRUCTURAL, PEST_CONTROL, CLEANING, SECURITY, INTERNET, NOISE, OTHER
            Possible PRIORITIES: HIGH, MEDIUM, LOW
            """.formatted(userInput);
    }

    private String buildCategoryPrompt(String title, String description, List<String> categoryCandidates) {
        String candidates = categoryCandidates.stream()
                .map(candidate -> "- " + candidate)
                .collect(Collectors.joining("\n"));

        return """
            You are an assistant for SmartResidential issue triage.
            Select the best issue category from the candidate list only.

            TITLE: %s
            DESCRIPTION: %s

            CANDIDATE CATEGORIES:
            %s

            Return ONLY a valid JSON object:
            {
              "categoryName": "one exact candidate category name or null",
              "confidence": 0.0,
              "reason": "short explanation"
            }
            """.formatted(
                title == null ? "" : title,
                description == null ? "" : description,
                candidates
        );
    }

    private String sendToOllama(String prompt) {
        String url = ollamaApiUrl + "/api/generate";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaModelName);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("response");
            }
        } catch (Exception e) {
            LOGGER.warning("Ollama request failed or timed out: " + e.getMessage());
            throw new IllegalStateException("Failed to communicate with Ollama: " + e.getMessage(), e);
        }
        return null;
    }

    private AIClassificationResult parseAIResponse(String aiResponse) {
        try {
            String cleanedResponse = aiResponse.trim();
            if (cleanedResponse.contains("{")) {
                cleanedResponse = cleanedResponse.substring(cleanedResponse.indexOf("{"));
                if (cleanedResponse.contains("}")) {
                    cleanedResponse = cleanedResponse.substring(0, cleanedResponse.lastIndexOf("}") + 1);
                }
            }

            JsonNode jsonNode = objectMapper.readTree(cleanedResponse);

            return new AIClassificationResult(
                    jsonNode.get("predictedCategory").asText(),
                    jsonNode.get("predictedPriority").asText(),
                    jsonNode.get("confidenceScore").asDouble()
            );
        } catch (Exception e) {
            return new AIClassificationResult("OTHER", "MEDIUM", 0.5);
        }
    }

    private IssueCategoryResult parseCategoryResponse(String aiResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(extractJson(aiResponse));
            String categoryName = jsonNode.path("categoryName").asText(null);
            if (categoryName == null || categoryName.isBlank()) {
                categoryName = jsonNode.path("predictedCategory").asText(null);
            }
            if (categoryName == null || categoryName.isBlank()) {
                categoryName = jsonNode.path("category").asText(null);
            }

            JsonNode confidenceNode = jsonNode.has("confidence")
                    ? jsonNode.get("confidence")
                    : jsonNode.get("confidenceScore");
            Double confidence = confidenceNode != null && confidenceNode.isNumber()
                    ? confidenceNode.asDouble()
                    : null;

            String reason = jsonNode.path("reason").asText(null);
            return new IssueCategoryResult(categoryName, confidence, reason);
        } catch (Exception e) {
            return new IssueCategoryResult(null, null, null);
        }
    }

    private String extractJson(String aiResponse) {
        if (aiResponse == null) {
            return "{}";
        }

        String cleanedResponse = aiResponse.trim();
        if (cleanedResponse.contains("{")) {
            cleanedResponse = cleanedResponse.substring(cleanedResponse.indexOf("{"));
            if (cleanedResponse.contains("}")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.lastIndexOf("}") + 1);
            }
        }
        return cleanedResponse;
    }

    public static class AIClassificationResult {
        private final String predictedCategory;
        private final String predictedPriority;
        private final Double confidenceScore;

        public AIClassificationResult(String predictedCategory, String predictedPriority, Double confidenceScore) {
            this.predictedCategory = predictedCategory;
            this.predictedPriority = predictedPriority;
            this.confidenceScore = confidenceScore;
        }

        public String getPredictedCategory() { return predictedCategory; }
        public String getPredictedPriority() { return predictedPriority; }
        public Double getConfidenceScore() { return confidenceScore; }
    }

    public static class IssueCategoryResult {
        private final String categoryName;
        private final Double confidence;
        private final String reason;

        public IssueCategoryResult(String categoryName, Double confidence, String reason) {
            this.categoryName = categoryName;
            this.confidence = confidence;
            this.reason = reason;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public Double getConfidence() {
            return confidence;
        }

        public String getReason() {
            return reason;
        }
    }
}
