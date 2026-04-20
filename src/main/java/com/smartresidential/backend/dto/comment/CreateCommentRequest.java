package com.smartresidential.backend.dto.comment;

import java.time.LocalDateTime;

public class CreateCommentRequest {
    private Long userId;
    private String content;
    private LocalDateTime timestamp;

    // Getter dhe Setter
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}