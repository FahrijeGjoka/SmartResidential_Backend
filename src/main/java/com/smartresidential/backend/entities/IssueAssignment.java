package com.smartresidential.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue_assignments")
public class IssueAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(optional = false)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    public IssueAssignment() {
    }

    @PrePersist
    public void prePersist() {
        this.assignedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Issue getIssue() {
        return issue;
    }

    public User getTechnician() {
        return technician;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public void setTechnician(User technician) {
        this.technician = technician;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
