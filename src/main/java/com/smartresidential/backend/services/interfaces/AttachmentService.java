package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.attachment.CreateAttachmentRequest;
import com.smartresidential.backend.dto.attachment.AttachmentResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    AttachmentResponseDTO createAttachment(CreateAttachmentRequest request);

    AttachmentResponseDTO uploadAttachment(Long issueId, MultipartFile file);

    List<AttachmentResponseDTO> getAllAttachments();

    AttachmentResponseDTO getAttachmentById(Long id);

    AttachmentResponseDTO getAttachmentStatus(Long id);

    void deleteAttachment(Long id);
}
