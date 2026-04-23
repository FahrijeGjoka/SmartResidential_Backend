package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.issueCategory.CreateIssueCategoryRequest;
import com.smartresidential.backend.dto.issueCategory.IssueCategoryResponseDTO;
import com.smartresidential.backend.dto.issueCategory.UpdateIssueCategoryRequest;

import java.util.List;

public interface IssueCategoryService {

    IssueCategoryResponseDTO createCategory(CreateIssueCategoryRequest request);

    IssueCategoryResponseDTO updateCategory(Long id, UpdateIssueCategoryRequest request);

    IssueCategoryResponseDTO getCategoryById(Long id);

    List<IssueCategoryResponseDTO> getAllCategories();

    void deleteCategory(Long id);
}