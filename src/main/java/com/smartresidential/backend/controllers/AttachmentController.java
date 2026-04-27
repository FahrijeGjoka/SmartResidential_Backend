package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.attachment.CreateAttachmentRequest;
import com.smartresidential.backend.dto.attachment.AttachmentResponseDTO;
import com.smartresidential.backend.services.interfaces.AttachmentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    // ✅ CREATE attachment
    @PostMapping
    public ResponseEntity<AttachmentResponseDTO> createAttachment(
            @RequestBody CreateAttachmentRequest request
    ) {
        return ResponseEntity.ok(
                attachmentService.createAttachment(request)
        );
    }

    // ✅ GET attachment by ID
    @GetMapping("/{id}")
    public ResponseEntity<AttachmentResponseDTO> getAttachmentById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                attachmentService.getAttachmentById(id)
        );
    }
}