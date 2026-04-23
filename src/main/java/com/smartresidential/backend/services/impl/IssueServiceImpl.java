package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.issue.CreateIssueRequest;
import com.smartresidential.backend.dto.issue.IssueResponseDTO;
import com.smartresidential.backend.dto.issue.UpdateIssueRequest;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.IssueAssignment;
import com.smartresidential.backend.entities.IssueCategory;
import com.smartresidential.backend.entities.IssueStatusHistory;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.repositories.ApartmentRepository;
import com.smartresidential.backend.repositories.IssueAssignmentRepository;
import com.smartresidential.backend.repositories.IssueCategoryRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.IssueStatusHistoryRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.IssueService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final IssueCategoryRepository issueCategoryRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final IssueAssignmentRepository issueAssignmentRepository;
    private final IssueStatusHistoryRepository issueStatusHistoryRepository;

    @Override
    public IssueResponseDTO createIssue(CreateIssueRequest request) {
        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Apartment not found with id: " + request.getApartmentId()
                ));

        User createdBy = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with id: " + request.getCreatedById()
                ));

        IssueCategory category = null;
        if (request.getCategoryId() != null) {
            category = issueCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Issue category not found with id: " + request.getCategoryId()
                    ));
        }

        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setStatus(request.getStatus());
        issue.setPriority(request.getPriority());
        issue.setApartment(apartment);
        issue.setCreatedBy(createdBy);
        issue.setCategory(category);

        Issue savedIssue = issueRepository.save(issue);
        return mapToResponse(savedIssue);
    }

    @Override
    public IssueResponseDTO updateIssue(Long id, UpdateIssueRequest request) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Issue not found with id: " + id));

        if (request.getTitle() != null) {
            issue.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            issue.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            issue.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }

        if (request.getCategoryId() != null) {
            IssueCategory category = issueCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Issue category not found with id: " + request.getCategoryId()
                    ));
            issue.setCategory(category);
        }

        Issue updatedIssue = issueRepository.save(issue);
        return mapToResponse(updatedIssue);
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponseDTO getIssueById(Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Issue not found with id: " + id));

        return mapToResponse(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponseDTO> getAllIssues() {
        return issueRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deleteIssue(Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Issue not found with id: " + id));

        issueRepository.delete(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponseDTO> getIssuesByStatus(String status) {
        return issueRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponseDTO> getIssuesByPriority(String priority) {
        return issueRepository.findByPriority(priority)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponseDTO> getIssuesByCategory(Long categoryId) {
        return issueRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponseDTO> getIssuesByApartment(Long apartmentId) {
        return issueRepository.findByApartmentId(apartmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponseDTO> getIssuesByCreatedBy(Long userId) {
        return issueRepository.findByCreatedById(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponseDTO> searchIssuesByTitle(String title) {
        return issueRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public IssueResponseDTO assignTechnician(Long issueId, Long technicianId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Issue not found with id: " + issueId));

        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + technicianId));

        IssueAssignment assignment = new IssueAssignment();
        assignment.setIssue(issue);
        assignment.setTechnician(technician);
        issueAssignmentRepository.save(assignment);

        issue.setStatus("ASSIGNED");
        Issue updatedIssue = issueRepository.save(issue);

        return mapToResponse(updatedIssue);
    }

    @Override
    public IssueResponseDTO changeStatus(Long issueId, String newStatus, Long changedByUserId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Issue not found with id: " + issueId));

        User changedBy = userRepository.findById(changedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + changedByUserId));

        String oldStatus = issue.getStatus();
        issue.setStatus(newStatus);

        Issue updatedIssue = issueRepository.save(issue);

        IssueStatusHistory history = new IssueStatusHistory();
        history.setIssue(issue);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        issueStatusHistoryRepository.save(history);

        return mapToResponse(updatedIssue);
    }

    private IssueResponseDTO mapToResponse(Issue issue) {
        IssueResponseDTO response = new IssueResponseDTO();
        response.setId(issue.getId());
        response.setTitle(issue.getTitle());
        response.setDescription(issue.getDescription());
        response.setStatus(issue.getStatus());
        response.setPriority(issue.getPriority());
        response.setApartmentId(issue.getApartment().getId());
        response.setCreatedById(issue.getCreatedBy().getId());
        response.setCategoryId(issue.getCategory() != null ? issue.getCategory().getId() : null);
        response.setCreatedAt(issue.getCreatedAt());
        response.setUpdatedAt(issue.getUpdatedAt());
        return response;
    }
}