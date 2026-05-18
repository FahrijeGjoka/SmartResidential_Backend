package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.issueCategory.CreateIssueCategoryRequest;
import com.smartresidential.backend.dto.issueCategory.IssueCategoryResponseDTO;
import com.smartresidential.backend.dto.issueCategory.UpdateIssueCategoryRequest;
import com.smartresidential.backend.services.interfaces.IssueCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issue-categories")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
public class IssueCategoryController {

    private final IssueCategoryService issueCategoryService;

    public IssueCategoryController(IssueCategoryService issueCategoryService) {
        this.issueCategoryService = issueCategoryService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<IssueCategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody UpdateIssueCategoryRequest request
    ) {
        return ResponseEntity.ok(issueCategoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        issueCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
