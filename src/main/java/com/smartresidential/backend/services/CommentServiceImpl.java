package com.smartresidential.backend.services;

import com.smartresidential.backend.entities.Comment;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.repositories.CommentRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import com.smartresidential.backend.dto.comment.CommentRequestDTO;
import com.smartresidential.backend.dto.comment.CommentResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(CommentRepository commentRepository,
                              IssueRepository issueRepository,
                              UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    // 🔹 CREATE COMMENT
    @Override
    public CommentResponseDTO createComment(CommentRequestDTO dto) {

        Issue issue = issueRepository.findById(dto.getIssueId())
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setUser(user);
        comment.setContent(dto.getContent());

        Comment saved = commentRepository.save(comment);

        return mapToResponse(saved);
    }

    // 🔹 GET COMMENTS BY ISSUE
    @Override
    public List<CommentResponseDTO> getCommentsByIssue(Long issueId) {
        return commentRepository.findByIssueId(issueId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 🔹 DELETE COMMENT
    @Override
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found");
        }
        commentRepository.deleteById(id);
    }

    // 🔹 MAPPER (shumë e rëndësishme për intervista 🔥)
    private CommentResponseDTO mapToResponse(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setUserId(comment.getUser().getId());
        return dto;
    }
}