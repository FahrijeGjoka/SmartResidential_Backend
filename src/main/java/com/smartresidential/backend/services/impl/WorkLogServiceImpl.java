package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.workLog.CreateWorkLogRequest;
import com.smartresidential.backend.dto.workLog.WorkLogResponseDTO;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.entities.WorkLog;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.repositories.WorkLogRepository;
import com.smartresidential.backend.services.interfaces.WorkLogService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkLogServiceImpl implements WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;

    public WorkLogServiceImpl(
            WorkLogRepository workLogRepository,
            UserRepository userRepository,
            IssueRepository issueRepository
    ) {
        this.workLogRepository = workLogRepository;
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
    }

    @Override
    public WorkLogResponseDTO createWorkLog(CreateWorkLogRequest request) {

        User technician = userRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new IllegalArgumentException("Technician not found"));

        Issue issue = issueRepository.findById(request.getIssueId())
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }

        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
        double hoursSpent = duration.toMinutes() / 60.0;

        WorkLog workLog = new WorkLog();
        workLog.setTechnician(technician);
        workLog.setIssue(issue);
        workLog.setDescription(request.getDescription());
        workLog.setHoursSpent(hoursSpent);

        WorkLog savedWorkLog = workLogRepository.save(workLog);

        return convertToResponseDTO(savedWorkLog);
    }

    @Override
    public WorkLogResponseDTO getWorkLogById(Long id) {
        WorkLog workLog = workLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work log not found"));

        return convertToResponseDTO(workLog);
    }

    @Override
    public List<WorkLogResponseDTO> getAllWorkLogs() {
        return workLogRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkLogResponseDTO> getWorkLogsByIssueId(Long issueId) {
        return workLogRepository.findByIssueId(issueId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkLogResponseDTO> getWorkLogsByTechnicianId(Long technicianId) {
        return workLogRepository.findByTechnicianId(technicianId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteWorkLog(Long id) {
        if (!workLogRepository.existsById(id)) {
            throw new IllegalArgumentException("Work log not found");
        }

        workLogRepository.deleteById(id);
    }

    private WorkLogResponseDTO convertToResponseDTO(WorkLog workLog) {
        WorkLogResponseDTO dto = new WorkLogResponseDTO();

        dto.setId(workLog.getId());
        dto.setIssueId(workLog.getIssue().getId());
        dto.setTechnicianId(workLog.getTechnician().getId());
        dto.setDescription(workLog.getDescription());
        dto.setHoursSpent(workLog.getHoursSpent());
        dto.setCreatedAt(workLog.getCreatedAt());

        return dto;
    }
}