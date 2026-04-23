package com.smartresidential.backend.services;


import com.smartresidential.backend.dto.comment.CommentRequestDTO;
import com.smartresidential.backend.dto.comment.CommentResponseDTO;
import com.smartresidential.backend.dto.comment.CreateCommentRequest;
import com.smartresidential.backend.dto.comment.UpdateCommentRequest;

import java.util.List;

public interface CommentService {

    CommentResponseDTO createComment(CommentRequestDTO dto);

    List<CommentResponseDTO> getCommentsByIssue(Long issueId);

    void deleteComment(Long id);
}