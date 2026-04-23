package com.smartresidential.backend.services;

import com.smartresidential.backend.dto.attachment.CreateAttachmentRequest;
import com.smartresidential.backend.dto.attachment.AttachmentResponseDTO;

public interface AttachmentService {

    AttachmentResponseDTO createAttachment(CreateAttachmentRequest request);

    AttachmentResponseDTO getAttachmentById(Long id);
}