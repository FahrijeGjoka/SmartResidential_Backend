package com.smartresidential.backend.services;

import com.smartresidential.backend.dto.issue.CreateIssueRequest;
import com.smartresidential.backend.dto.issue.IssueResponseDTO;
import com.smartresidential.backend.dto.issue.UpdateIssueRequest;

import java.util.List;

public interface IssueService {

    IssueResponseDTO createIssue(CreateIssueRequest request);

    IssueResponseDTO updateIssue(Long id, UpdateIssueRequest request);

    IssueResponseDTO getIssueById(Long id);

    List<IssueResponseDTO> getAllIssues();

    void deleteIssue(Long id);

    List<IssueResponseDTO> getIssuesByStatus(String status);

    List<IssueResponseDTO> getIssuesByPriority(String priority);

    List<IssueResponseDTO> getIssuesByCategory(Long categoryId);

    List<IssueResponseDTO> getIssuesByApartment(Long apartmentId);

    List<IssueResponseDTO> getIssuesByCreatedBy(Long userId);

    List<IssueResponseDTO> searchIssuesByTitle(String title);

    IssueResponseDTO assignTechnician(Long issueId, Long technicianId);

    IssueResponseDTO changeStatus(Long issueId, String newStatus, Long changedByUserId);
}