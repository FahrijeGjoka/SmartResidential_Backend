package com.smartresidential.backend.services;

import com.smartresidential.backend.dto.worklog.CreateWorkLogRequest;
import com.smartresidential.backend.dto.worklog.WorkLogResponseDTO;
import com.smartresidential.backend.entities.WorkLog;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.repositories.WorkLogRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.services.WorkLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WorkLogServiceImpl implements WorkLogService {

    @Autowired
    private WorkLogRepository workLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Override
    public WorkLogResponseDTO createWorkLog(CreateWorkLogRequest request) {
        // Validate User
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found");
        }

        // Validate Issue
        Optional<Issue> issueOptional = issueRepository.findById(request.getUserId());  // Assuming the issue is linked with the user
        if (!issueOptional.isPresent()) {
            throw new IllegalArgumentException("Issue not found");
        }

        // Calculate hours spent (difference between start and end time)
        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
        double hoursSpent = duration.toMinutes() / 60.0;

        // Create WorkLog entity
        WorkLog workLog = new WorkLog();
        workLog.setTechnician(userOptional.get());
        workLog.setIssue(issueOptional.get());
        workLog.setDescription(request.getDescription());
        workLog.setHoursSpent(hoursSpent);

        // Save WorkLog entity
        WorkLog savedWorkLog = workLogRepository.save(workLog);

        // Return WorkLogResponseDTO
        return convertToResponseDTO(savedWorkLog);
    }

    @Override
    public WorkLogResponseDTO getWorkLogById(Long id) {
        Optional<WorkLog> workLogOptional = workLogRepository.findById(id);
        if (!workLogOptional.isPresent()) {
            throw new IllegalArgumentException("Work log not found");
        }
        return convertToResponseDTO(workLogOptional.get());
    }

    private WorkLogResponseDTO convertToResponseDTO(WorkLog workLog) {
        WorkLogResponseDTO dto = new WorkLogResponseDTO();
        dto.setId(workLog.getId());
        dto.setUserId(workLog.getTechnician().getId());
        dto.setDescription(workLog.getDescription());
        dto.setStartTime(workLog.getCreatedAt());  // Assuming 'createdAt' represents the start time
        dto.setEndTime(workLog.getCreatedAt().plusHours(workLog.getHoursSpent().longValue()));  // Calculate end time based on hoursSpent
        return dto;
    }
}