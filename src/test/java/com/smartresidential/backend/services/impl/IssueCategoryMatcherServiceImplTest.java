package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.ai.IssueCategoryMatch;
import com.smartresidential.backend.entities.IssueCategory;
import com.smartresidential.backend.repositories.IssueCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueCategoryMatcherServiceImplTest {

    @Mock
    private IssueCategoryRepository issueCategoryRepository;

    @Mock
    private OllamaService ollamaService;

    @Test
    void usesOllamaCategoryWhenItMatchesExistingCategory() {
        IssueCategory plumbing = category(1L, "Plumbing", "Leaks and drains", "Plumbing");
        IssueCategory electrical = category(2L, "Electrical", "Lights and wiring", "Electrical");
        IssueCategoryMatcherServiceImpl service =
                new IssueCategoryMatcherServiceImpl(issueCategoryRepository, ollamaService);

        when(issueCategoryRepository.findAll()).thenReturn(List.of(plumbing, electrical));
        when(ollamaService.classifyIssueCategory(
                "Bathroom leak",
                "Water is leaking under the sink",
                List.of("Plumbing", "Electrical")
        )).thenReturn(new OllamaService.IssueCategoryResult(
                "Plumbing",
                0.91,
                "The description mentions leaking water under a sink."
        ));

        Optional<IssueCategoryMatch> match =
                service.matchCategory("Bathroom leak", "Water is leaking under the sink");

        assertThat(match).isPresent();
        assertThat(match.get().getCategory()).isSameAs(plumbing);
        assertThat(match.get().getConfidence()).isEqualTo(0.91);
        assertThat(match.get().getReason()).contains("leaking water");
    }

    @Test
    void fallsBackToKeywordMatchingWhenOllamaFails() {
        IssueCategory plumbing = category(1L, "Plumbing", "Leaks and drains", "Plumbing");
        IssueCategoryMatcherServiceImpl service =
                new IssueCategoryMatcherServiceImpl(issueCategoryRepository, ollamaService);

        when(issueCategoryRepository.findAll()).thenReturn(List.of(plumbing));
        when(ollamaService.classifyIssueCategory(
                "Bathroom leak",
                "Water is leaking under the sink",
                List.of("Plumbing")
        )).thenThrow(new IllegalStateException("Ollama unavailable"));

        Optional<IssueCategoryMatch> match =
                service.matchCategory("Bathroom leak", "Water is leaking under the sink");

        assertThat(match).isPresent();
        assertThat(match.get().getCategory()).isSameAs(plumbing);
    }

    private IssueCategory category(Long id, String name, String description, String requiredSpecialization) {
        IssueCategory category = new IssueCategory();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        category.setRequiredSpecialization(requiredSpecialization);
        return category;
    }
}
