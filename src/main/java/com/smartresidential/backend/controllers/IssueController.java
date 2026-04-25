package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.issue.CreateIssueRequest;
import com.smartresidential.backend.dto.issue.IssueResponseDTO;
import com.smartresidential.backend.dto.issue.UpdateIssueRequest;
import com.smartresidential.backend.dto.issueAssignment.CreateIssueAssignmentRequest;
import com.smartresidential.backend.dto.issueStatusHistory.CreateIssueStatusHistoryRequest;
import com.smartresidential.backend.services.interfaces.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @PostMapping
    public ResponseEntity<IssueResponseDTO> createIssue(@RequestBody CreateIssueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(issueService.createIssue(request));
    }

    @GetMapping
    public ResponseEntity<List<IssueResponseDTO>> getAllIssues(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long apartmentId,
            @RequestParam(required = false) Long createdById,
            @RequestParam(required = false) String title
    ) {
        if (status != null) {
            return ResponseEntity.ok(issueService.getIssuesByStatus(status));
        }

        if (priority != null) {
            return ResponseEntity.ok(issueService.getIssuesByPriority(priority));
        }

        if (categoryId != null) {
            return ResponseEntity.ok(issueService.getIssuesByCategory(categoryId));
        }

        if (apartmentId != null) {
            return ResponseEntity.ok(issueService.getIssuesByApartment(apartmentId));
        }

        if (createdById != null) {
            return ResponseEntity.ok(issueService.getIssuesByCreatedBy(createdById));
        }

        if (title != null) {
            return ResponseEntity.ok(issueService.searchIssuesByTitle(title));
        }

        return ResponseEntity.ok(issueService.getAllIssues());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueResponseDTO> getIssueById(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IssueResponseDTO> updateIssue(
            @PathVariable Long id,
            @RequestBody UpdateIssueRequest request
    ) {
        return ResponseEntity.ok(issueService.updateIssue(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIssue(@PathVariable Long id) {
        issueService.deleteIssue(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<IssueResponseDTO> changeStatus(
            @PathVariable Long id,
            @RequestBody CreateIssueStatusHistoryRequest request
    ) {
        return ResponseEntity.ok(
                issueService.changeStatus(
                        id,
                        request.getNewStatus(),
                        request.getChangedByUserId()
                )
        );
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<IssueResponseDTO> assignTechnician(
            @PathVariable Long id,
            @RequestBody CreateIssueAssignmentRequest request
    ) {
        return ResponseEntity.ok(
                issueService.assignTechnician(id, request.getTechnicianId())
        );
    }
}