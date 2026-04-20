package com.smartresidential.backend.dto.comment;

import java.time.LocalDateTime;

public class UpdateCommentRequest {
    private Long id;  // ID e komentit për t'u azhurnuar
    private String content;
    private LocalDateTime timestamp;

    // Getter dhe Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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