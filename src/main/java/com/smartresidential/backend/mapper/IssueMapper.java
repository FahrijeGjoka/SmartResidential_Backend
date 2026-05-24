package com.smartresidential.backend.mapper;

import com.smartresidential.backend.dto.issue.IssueResponseDTO;
import com.smartresidential.backend.entities.AIClassificationStatus;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.IssueAssignment;
import com.smartresidential.backend.entities.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IssueMapper {

    private static final String STATUS_ASSIGNED = "ASSIGNED";
    private static final String STATUS_OPEN = "OPEN";

    public IssueResponseDTO toResponse(Issue issue, Optional<IssueAssignment> currentAssignment) {
        IssueResponseDTO response = new IssueResponseDTO();
        response.setId(issue.getId());
        response.setTitle(issue.getTitle());
        response.setDescription(issue.getDescription());
        response.setPriority(issue.getPriority());
        response.setApartmentId(issue.getApartment() != null ? issue.getApartment().getId() : null);
        response.setCreatedById(issue.getCreatedBy() != null ? issue.getCreatedBy().getId() : null);
        response.setCategoryId(issue.getCategory() != null ? issue.getCategory().getId() : null);
        response.setCategoryName(issue.getCategory() != null ? issue.getCategory().getName() : null);
        response.setAiCategoryConfidence(issue.getAiCategoryConfidence());
        response.setAiCategoryReason(issue.getAiCategoryReason());
        response.setAiClassificationStatus(resolveAiClassificationStatus(issue).name());
        response.setStatus(STATUS_ASSIGNED.equals(issue.getStatus()) && currentAssignment.isEmpty()
                ? STATUS_OPEN
                : issue.getStatus());
        currentAssignment.ifPresent(assignment -> {
            Long technicianId = assignment.getTechnician().getId();
            response.setAssignedTechnicianId(technicianId);
            response.setAssignedTechnicianUserId(technicianId);
            response.setAssignedTechnicianName(formatTechnicianName(assignment.getTechnician()));
        });
        response.setCreatedAt(issue.getCreatedAt());
        response.setUpdatedAt(issue.getUpdatedAt());
        return response;
    }

    private AIClassificationStatus resolveAiClassificationStatus(Issue issue) {
        if (issue.getAiClassificationStatus() != null) {
            return issue.getAiClassificationStatus();
        }

        return issue.getCategory() == null
                ? AIClassificationStatus.NEEDS_REVIEW
                : AIClassificationStatus.COMPLETED;
    }

    private String formatTechnicianName(User technician) {
        if (technician == null) {
            return null;
        }

        String name = String.join(" ",
                technician.getFirstName() == null ? "" : technician.getFirstName(),
                technician.getLastName() == null ? "" : technician.getLastName()
        ).trim();

        return name.isBlank() ? technician.getEmail() : name;
    }
}
