package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.ai.IssueCategoryMatch;
import com.smartresidential.backend.dto.ai.IssueCategoryMatchResponse;
import com.smartresidential.backend.entities.IssueCategory;
import com.smartresidential.backend.repositories.IssueCategoryRepository;
import com.smartresidential.backend.services.interfaces.IssueCategoryMatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssueCategoryMatcherServiceImpl implements IssueCategoryMatcherService {

    private static final double MIN_CONFIDENCE = 0.4;
    private static final Logger LOGGER = Logger.getLogger(IssueCategoryMatcherServiceImpl.class.getName());

    private final IssueCategoryRepository issueCategoryRepository;
    private final OllamaService ollamaService;

    @Override
    public Optional<IssueCategoryMatch> matchCategory(String title, String description) {
        String input = normalize((title == null ? "" : title) + " " + (description == null ? "" : description));
        if (input.isBlank()) {
            return Optional.empty();
        }

        var categories = issueCategoryRepository.findAll();
        Optional<IssueCategoryMatch> ollamaMatch = matchWithOllama(title, description, categories);
        if (ollamaMatch.isPresent()) {
            return ollamaMatch;
        }

        Set<String> inputTokens = tokenize(input);
        return categories.stream()
                .map(category -> score(category, input, inputTokens))
                .filter(match -> match.getConfidence() >= MIN_CONFIDENCE)
                .max(Comparator.comparingDouble(IssueCategoryMatch::getConfidence));
    }

    @Override
    public IssueCategoryMatchResponse matchCategoryForResponse(String title, String description) {
        return matchCategory(title, description)
                .map(match -> new IssueCategoryMatchResponse(
                        match.getCategory().getId(),
                        match.getCategory().getName(),
                        match.getConfidence(),
                        match.getReason()
                ))
                .orElseGet(() -> new IssueCategoryMatchResponse(
                        null,
                        null,
                        0.0,
                        "No issue category matched the title and description with enough confidence."
                ));
    }

    private Optional<IssueCategoryMatch> matchWithOllama(String title, String description, java.util.List<IssueCategory> categories) {
        if (categories.isEmpty()) {
            return Optional.empty();
        }

        try {
            OllamaService.IssueCategoryResult result = ollamaService.classifyIssueCategory(
                    title,
                    description,
                    categories.stream().map(IssueCategory::getName).toList()
            );

            if (result.getCategoryName() == null || result.getConfidence() == null
                    || result.getConfidence() < MIN_CONFIDENCE) {
                return Optional.empty();
            }

            return categories.stream()
                    .filter(category -> category.getName() != null
                            && category.getName().equalsIgnoreCase(result.getCategoryName().trim()))
                    .findFirst()
                    .map(category -> new IssueCategoryMatch(
                            category,
                            result.getConfidence(),
                            result.getReason() == null || result.getReason().isBlank()
                                    ? "Ollama matched the issue to " + category.getName() + "."
                                    : result.getReason()
                    ));
        } catch (RuntimeException exception) {
            LOGGER.warning("AI issue category matching failed; falling back to keyword matching: "
                    + exception.getMessage());
            return Optional.empty();
        }
    }

    private IssueCategoryMatch score(IssueCategory category, String input, Set<String> inputTokens) {
        String categoryText = normalize(String.join(" ",
                nullToBlank(category.getName()),
                nullToBlank(category.getDescription()),
                nullToBlank(category.getRequiredSpecialization())
        ));

        Set<String> categoryTokens = tokenize(categoryText);
        long matchingTokens = categoryTokens.stream()
                .filter(inputTokens::contains)
                .count();

        double score = categoryTokens.isEmpty()
                ? 0.0
                : Math.min(0.95, 0.25 + (matchingTokens * 0.18));

        if (category.getName() != null && !category.getName().isBlank() && input.contains(normalize(category.getName()))) {
            score = Math.max(score, 0.9);
        }

        String reason = score >= MIN_CONFIDENCE
                ? "Matched issue text against category keywords for " + category.getName() + "."
                : "Category keywords did not match strongly enough.";

        return new IssueCategoryMatch(category, score, reason);
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.split("[^a-z0-9]+"))
                .map(this::normalizeToken)
                .filter(token -> token.length() > 2)
                .collect(Collectors.toSet());
    }

    private String normalizeToken(String token) {
        if (token.endsWith("ing") && token.length() > 5) {
            return token.substring(0, token.length() - 3);
        }

        if (token.endsWith("s") && token.length() > 4) {
            return token.substring(0, token.length() - 1);
        }

        return token;
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
