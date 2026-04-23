package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.attachment.CreateAttachmentRequest;
import com.smartresidential.backend.dto.attachment.AttachmentResponseDTO;
import com.smartresidential.backend.entities.Attachment;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.repositories.AttachmentRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.services.interfaces.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Override
    public AttachmentResponseDTO createAttachment(CreateAttachmentRequest request) {
        // Validate Issue
        Optional<Issue> issueOptional = issueRepository.findById(request.getRelatedEntityId());
        if (!issueOptional.isPresent()) {
            throw new IllegalArgumentException("Issue not found");
        }

        // Create Attachment entity
        Attachment attachment = new Attachment();
        attachment.setIssue(issueOptional.get());
        attachment.setFileName(request.getFileName());
        attachment.setFileUrl("path/to/storage/" + request.getFileName()); // For simplicity, you can change this logic to handle actual file storage
        attachment.setUploadedAt(null);  // Automatically set in prePersist

        // Save Attachment entity
        Attachment savedAttachment = attachmentRepository.save(attachment);

        // Return AttachmentResponseDTO
        return convertToResponseDTO(savedAttachment);
    }

    @Override
    public AttachmentResponseDTO getAttachmentById(Long id) {
        Optional<Attachment> attachmentOptional = attachmentRepository.findById(id);
        if (!attachmentOptional.isPresent()) {
            throw new IllegalArgumentException("Attachment not found");
        }
        return convertToResponseDTO(attachmentOptional.get());
    }

    private AttachmentResponseDTO convertToResponseDTO(Attachment attachment) {
        AttachmentResponseDTO dto = new AttachmentResponseDTO();
        dto.setId(attachment.getId());
        dto.setRelatedEntityId(attachment.getIssue().getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileType("unknown");  // This can be set based on the file type if required
        return dto;
    }
}