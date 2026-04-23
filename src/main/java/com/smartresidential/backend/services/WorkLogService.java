package com.smartresidential.backend.services;

import com.smartresidential.backend.dto.worklog.CreateWorkLogRequest;
import com.smartresidential.backend.dto.worklog.WorkLogResponseDTO;

public interface WorkLogService {

    WorkLogResponseDTO createWorkLog(CreateWorkLogRequest request);

    WorkLogResponseDTO getWorkLogById(Long id);
}