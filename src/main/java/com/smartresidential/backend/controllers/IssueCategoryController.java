package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.issueCategory.CreateIssueCategoryRequest;
import com.smartresidential.backend.dto.issueCategory.IssueCategoryResponseDTO;
import com.smartresidential.backend.dto.issueCategory.UpdateIssueCategoryRequest;
import com.smartresidential.backend.services.interfaces.IssueCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issue-categories")
@RequiredArgsConstructor
public class IssueCategoryController {

    private final IssueCategoryService issueCategoryService;

    @PostMapping
    public ResponseEntity<IssueCategoryResponseDTO> createCategory(
            @RequestBody CreateIssueCategoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(issueCategoryService.createCategory(request));
    }

    @GetMapping
    public ResponseEntity<List<IssueCategoryResponseDTO>> getAllCategories() {
        return ResponseEntity.ok(issueCategoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueCategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(issueCategoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IssueCategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody UpdateIssueCategoryRequest request
    ) {
        return ResponseEntity.ok(issueCategoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        issueCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}