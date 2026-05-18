package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.attachment.AttachmentResponseDTO;
import com.smartresidential.backend.dto.attachment.CreateAttachmentRequest;
import com.smartresidential.backend.services.interfaces.AttachmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping
    public ResponseEntity<AttachmentResponseDTO> createAttachment(
            @RequestBody CreateAttachmentRequest request
    ) {
        return ResponseEntity.ok(
                attachmentService.createAttachment(request)
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<AttachmentResponseDTO> uploadAttachment(
            @RequestParam Long issueId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(attachmentService.uploadAttachment(issueId, file));
    }

    @GetMapping
    public ResponseEntity<List<AttachmentResponseDTO>> getAttachments() {
        return ResponseEntity.ok(attachmentService.getAllAttachments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttachmentResponseDTO> getAttachmentById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                attachmentService.getAttachmentById(id)
        );
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<AttachmentResponseDTO> getAttachmentStatus(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                attachmentService.getAttachmentStatus(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long id
    ) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }
}
