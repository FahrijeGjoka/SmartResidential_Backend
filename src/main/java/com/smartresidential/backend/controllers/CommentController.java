package com.smartresidential.backend.controllers;


import com.smartresidential.backend.dto.comment.CommentRequestDTO;
import com.smartresidential.backend.dto.comment.CommentResponseDTO;
import com.smartresidential.backend.services.interfaces.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 🔹 POST /api/issues/{id}/comments
    @PostMapping("/{issueId}/comments")
    public ResponseEntity<CommentResponseDTO> createComment(
            @PathVariable Long issueId,
            @RequestBody CommentRequestDTO dto
    ) {
        dto.setIssueId(issueId); // lidhim comment me issue nga path
        CommentResponseDTO response = commentService.createComment(dto);
        return ResponseEntity.ok(response);
    }

    // 🔹 GET /api/issues/{id}/comments
    @GetMapping("/{issueId}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByIssue(
            @PathVariable Long issueId
    ) {
        List<CommentResponseDTO> comments = commentService.getCommentsByIssue(issueId);
        return ResponseEntity.ok(comments);
    }

    // 🔹 DELETE /api/issues/{id}/comments/{commentId}
    @DeleteMapping("/{issueId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long issueId,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}