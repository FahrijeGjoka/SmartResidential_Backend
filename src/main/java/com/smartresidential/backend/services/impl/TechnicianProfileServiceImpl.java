package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.common.PageRequestFactory;
import com.smartresidential.backend.dto.technicianProfile.CreateTechnicianProfileRequest;
import com.smartresidential.backend.dto.technicianProfile.TechnicianProfileFilterRequest;
import com.smartresidential.backend.dto.technicianProfile.TechnicianProfileResponseDTO;
import com.smartresidential.backend.dto.technicianProfile.UpdateTechnicianProfileRequest;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.IssueAssignment;
import com.smartresidential.backend.entities.TechnicianProfile;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.repositories.IssueAssignmentRepository;
import com.smartresidential.backend.repositories.TechnicianProfileRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.TechnicianProfileService;
import com.smartresidential.backend.specifications.TechnicianProfileSpecification;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class TechnicianProfileServiceImpl implements TechnicianProfileService {

    private static final int DEFAULT_MAX_ACTIVE_ISSUES = 5;
    private static final Set<String> ACTIVE_WORKLOAD_STATUSES = Set.of("ASSIGNED", "IN_PROGRESS");
    private static final Set<String> HIGH_PRIORITY_VALUES = Set.of("HIGH", "URGENT");

    private final TechnicianProfileRepository repository;
    private final UserRepository userRepository;
    private final IssueAssignmentRepository issueAssignmentRepository;

    public TechnicianProfileServiceImpl(TechnicianProfileRepository repository,
                                        UserRepository userRepository,
                                        IssueAssignmentRepository issueAssignmentRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.issueAssignmentRepository = issueAssignmentRepository;
    }

    @Override
    public TechnicianProfileResponseDTO create(CreateTechnicianProfileRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TechnicianProfile profile = new TechnicianProfile();
        profile.setUser(user);
        profile.setSpecialization(request.getSpecialization());
        profile.setIsAvailable(request.getIsAvailable());
        profile.setMaxActiveIssues(resolveMaxActiveIssues(request.getMaxActiveIssues()));

        return mapToDTO(repository.save(profile));
    }

    @Override
    public TechnicianProfileResponseDTO getById(Long id) {
        return repository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    @Override
    public TechnicianProfileResponseDTO getByUserId(Long userId) {
        return repository.findByUserId(userId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    @Override
    public List<TechnicianProfileResponseDTO> getAll() {
        return repository.findAll()
                .stream().map(this::mapToDTO).toList();
    }

    @Override
    public Page<TechnicianProfileResponseDTO> search(TechnicianProfileFilterRequest filter) {
        return repository.findAll(
                TechnicianProfileSpecification.withFilters(filter),
                PageRequestFactory.from(filter, "id")
        ).map(this::mapToDTO);
    }

    @Override
    public List<TechnicianProfileResponseDTO> getAvailable() {
        return repository.findByIsAvailableTrue()
                .stream()
                .filter(this::isUnderCapacity)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<TechnicianProfileResponseDTO> getBySpecialization(String specialization) {
        return repository.findBySpecialization(specialization)
                .stream().map(this::mapToDTO).toList();
    }

    @Override
    public TechnicianProfileResponseDTO update(Long id, UpdateTechnicianProfileRequest request) {
        TechnicianProfile profile = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        if (request.getSpecialization() != null)
            profile.setSpecialization(request.getSpecialization());

        if (request.getIsAvailable() != null)
            profile.setIsAvailable(request.getIsAvailable());

        if (request.getMaxActiveIssues() != null)
            profile.setMaxActiveIssues(resolveMaxActiveIssues(request.getMaxActiveIssues()));

        return mapToDTO(repository.save(profile));
    }

    @Override
    public void changeAvailability(Long id, Boolean isAvailable) {
        TechnicianProfile profile = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        profile.setIsAvailable(isAvailable);
        repository.save(profile);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private TechnicianProfileResponseDTO mapToDTO(TechnicianProfile p) {
        TechnicianProfileResponseDTO dto = new TechnicianProfileResponseDTO();
        dto.setId(p.getId());
        dto.setUserId(p.getUser().getId());
        dto.setSpecialization(p.getSpecialization());
        dto.setIsAvailable(p.getIsAvailable());
        dto.setMaxActiveIssues(resolveMaxActiveIssues(p.getMaxActiveIssues()));
        dto.setActiveIssueCount(activeWorkload(p, false));
        dto.setActiveHighPriorityIssueCount(activeWorkload(p, true));
        dto.setLastAssignedAt(latestAssignedAt(p));
        return dto;
    }

    private int resolveMaxActiveIssues(Integer maxActiveIssues) {
        return maxActiveIssues == null || maxActiveIssues < 1
                ? DEFAULT_MAX_ACTIVE_ISSUES
                : maxActiveIssues;
    }

    private int activeWorkload(TechnicianProfile profile, boolean highPriorityOnly) {
        return (int) issueAssignmentRepository.findByTechnicianId(profile.getUser().getId())
                .stream()
                .map(IssueAssignment::getIssue)
                .filter(issue -> issue != null
                        && !Boolean.TRUE.equals(issue.getArchived())
                        && ACTIVE_WORKLOAD_STATUSES.contains(issue.getStatus()))
                .filter(issue -> !highPriorityOnly || isHighPriority(issue))
                .count();
    }

    private boolean isHighPriority(Issue issue) {
        return issue.getPriority() != null && HIGH_PRIORITY_VALUES.contains(issue.getPriority().toUpperCase());
    }

    private LocalDateTime latestAssignedAt(TechnicianProfile profile) {
        return issueAssignmentRepository.findByTechnicianId(profile.getUser().getId())
                .stream()
                .map(this::assignmentTimestamp)
                .filter(timestamp -> timestamp != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private boolean isUnderCapacity(TechnicianProfile profile) {
        return activeWorkload(profile, false) < resolveMaxActiveIssues(profile.getMaxActiveIssues());
    }

    private LocalDateTime assignmentTimestamp(IssueAssignment assignment) {
        if (assignment.getAssignedAt() != null) {
            return assignment.getAssignedAt();
        }

        Issue issue = assignment.getIssue();
        if (issue == null) {
            return null;
        }

        if (issue.getUpdatedAt() != null) {
            return issue.getUpdatedAt();
        }

        return issue.getCreatedAt();
    }
}
