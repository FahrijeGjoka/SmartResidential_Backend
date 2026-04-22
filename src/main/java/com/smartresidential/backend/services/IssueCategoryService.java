package com.smartresidential.backend.services;

import com.smartresidential.backend.dto.issueCategory.CreateIssueCategoryRequest;
import com.smartresidential.backend.dto.issueCategory.IssueCategoryResponseDTO;
import com.smartresidential.backend.dto.issueCategory.UpdateIssueCategoryRequest;
import com.smartresidential.backend.entities.IssueCategory;
import com.smartresidential.backend.repositories.IssueCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueCategoryService {

    private final IssueCategoryRepository issueCategoryRepository;

    public IssueCategoryResponseDTO createCategory(CreateIssueCategoryRequest request) {
        if (issueCategoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Issue category with this name already exists.");
        }

        IssueCategory category = new IssueCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        IssueCategory savedCategory = issueCategoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    public IssueCategoryResponseDTO updateCategory(Long id, UpdateIssueCategoryRequest request) {
        IssueCategory category = issueCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Issue category not found with id: " + id));

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (issueCategoryRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Issue category with this name already exists.");
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        IssueCategory updatedCategory = issueCategoryRepository.save(category);
        return mapToResponse(updatedCategory);
    }

    @Transactional(readOnly = true)
    public IssueCategoryResponseDTO getCategoryById(Long id) {
        IssueCategory category = issueCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Issue category not found with id: " + id));

        return mapToResponse(category);
    }

    @Transactional(readOnly = true)
    public List<IssueCategoryResponseDTO> getAllCategories() {
        return issueCategoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteCategory(Long id) {
        IssueCategory category = issueCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Issue category not found with id: " + id));

        issueCategoryRepository.delete(category);
    }

    private IssueCategoryResponseDTO mapToResponse(IssueCategory category) {
        IssueCategoryResponseDTO response = new IssueCategoryResponseDTO();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        return response;
    }
}