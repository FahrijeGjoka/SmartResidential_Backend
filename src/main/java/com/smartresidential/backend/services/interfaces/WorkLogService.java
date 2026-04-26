package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.workLog.CreateWorkLogRequest;
import com.smartresidential.backend.dto.workLog.WorkLogResponseDTO;

import java.util.List;

public interface WorkLogService {

    WorkLogResponseDTO createWorkLog(CreateWorkLogRequest request);

    WorkLogResponseDTO getWorkLogById(Long id);

    List<WorkLogResponseDTO> getAllWorkLogs();

    List<WorkLogResponseDTO> getWorkLogsByIssueId(Long issueId);

    List<WorkLogResponseDTO> getWorkLogsByTechnicianId(Long technicianId);

    void deleteWorkLog(Long id);
}