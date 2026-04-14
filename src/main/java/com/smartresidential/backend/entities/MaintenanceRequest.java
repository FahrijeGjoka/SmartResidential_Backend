package com.smartresidential.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_requests")
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "issue_id", nullable = false, unique = true)
    private Issue issue;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Column(name = "request_note", columnDefinition = "TEXT")
    private String requestNote;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    public MaintenanceRequest() {
    }

    @PrePersist
    public void prePersist() {
        this.requestedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Issue getIssue() {
        return issue;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public String getRequestNote() {
        return requestNote;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }

    public void setRequestNote(String requestNote) {
        this.requestNote = requestNote;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
}

