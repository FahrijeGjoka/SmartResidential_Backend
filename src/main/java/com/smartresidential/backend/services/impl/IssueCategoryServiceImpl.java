package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.cache.CacheNames;
import com.smartresidential.backend.dto.issueCategory.CreateIssueCategoryRequest;
import com.smartresidential.backend.dto.issueCategory.IssueCategoryResponseDTO;
import com.smartresidential.backend.dto.issueCategory.UpdateIssueCategoryRequest;
import com.smartresidential.backend.entities.IssueCategory;
import com.smartresidential.backend.exceptions.ConflictException;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.repositories.IssueCategoryRepository;
import com.smartresidential.backend.services.interfaces.IssueCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueCategoryServiceImpl implements IssueCategoryService {

    private final IssueCategoryRepository issueCategoryRepository;

    @Override
    @CacheEvict(
            cacheNames = CacheNames.ISSUE_CATEGORIES,
            key = "T(com.smartresidential.backend.cache.TenantCacheKeys).all('issue-categories')"
    )
    public IssueCategoryResponseDTO createCategory(CreateIssueCategoryRequest request) {
        if (issueCategoryRepository.existsByName(request.getName())) {
            throw new ConflictException("Issue category with this name already exists.");
        }

        IssueCategory category = new IssueCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setRequiredSpecialization(normalizeSpecialization(request.getRequiredSpecialization()));

        IssueCategory savedCategory = issueCategoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = CacheNames.ISSUE_CATEGORIES,
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).all('issue-categories')"
            ),
            @CacheEvict(
                    cacheNames = CacheNames.ISSUE_CATEGORIES,
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).byId('issue-categories', #id)"
            )
    })
    public IssueCategoryResponseDTO updateCategory(Long id, UpdateIssueCategoryRequest request) {
        IssueCategory category = issueCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue category not found with id: " + id));

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (issueCategoryRepository.existsByName(request.getName())) {
                throw new ConflictException("Issue category with this name already exists.");
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getRequiredSpecialization() != null) {
            category.setRequiredSpecialization(normalizeSpecialization(request.getRequiredSpecialization()));
        }

        IssueCategory updatedCategory = issueCategoryRepository.save(category);
        return mapToResponse(updatedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUE_CATEGORIES,
            key = "T(com.smartresidential.backend.cache.TenantCacheKeys).byId('issue-categories', #id)"
    )
    public IssueCategoryResponseDTO getCategoryById(Long id) {
        IssueCategory category = issueCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue category not found with id: " + id));

        return mapToResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.ISSUE_CATEGORIES,
            key = "T(com.smartresidential.backend.cache.TenantCacheKeys).all('issue-categories')"
    )
    public List<IssueCategoryResponseDTO> getAllCategories() {
        return issueCategoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = CacheNames.ISSUE_CATEGORIES,
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).all('issue-categories')"
            ),
            @CacheEvict(
                    cacheNames = CacheNames.ISSUE_CATEGORIES,
                    key = "T(com.smartresidential.backend.cache.TenantCacheKeys).byId('issue-categories', #id)"
            )
    })
    public void deleteCategory(Long id) {
        IssueCategory category = issueCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue category not found with id: " + id));

        issueCategoryRepository.delete(category);
    }

    private IssueCategoryResponseDTO mapToResponse(IssueCategory category) {
        IssueCategoryResponseDTO response = new IssueCategoryResponseDTO();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setRequiredSpecialization(category.getRequiredSpecialization());
        return response;
    }

    private String normalizeSpecialization(String specialization) {
        if (specialization == null || specialization.isBlank()) {
            return null;
        }

        return specialization.trim();
    }
}
