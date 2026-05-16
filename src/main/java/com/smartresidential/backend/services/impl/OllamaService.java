package com.smartresidential.backend.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaService {

    @Value("${ollama.api.url:http://localhost:11434}")
    private String ollamaApiUrl;

    @Value("${ollama.model.name:llama3.2:3b}")
    private String ollamaModelName;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OllamaService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public AIClassificationResult classifyIssue(String issueDescription) {
        String prompt = buildClassificationPrompt(issueDescription);
        String aiResponse = sendToOllama(prompt);
        return parseAIResponse(aiResponse);
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
}
