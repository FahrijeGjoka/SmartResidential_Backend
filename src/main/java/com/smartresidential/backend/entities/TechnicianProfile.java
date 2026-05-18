package com.smartresidential.backend.entities;
//ndryshime
import jakarta.persistence.*;

@Entity
@Table(name = "technician_profiles")
public class TechnicianProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String specialization;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "max_active_issues")
    private Integer maxActiveIssues = 5;

    public TechnicianProfile() {
    }

    @PrePersist
    public void prePersist() {
        if (this.isAvailable == null) {
            this.isAvailable = true;
        }
        if (this.maxActiveIssues == null) {
            this.maxActiveIssues = 5;
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getSpecialization() {
        return specialization;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public Integer getMaxActiveIssues() {
        return maxActiveIssues;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setIsAvailable(Boolean available) {
        isAvailable = available;
    }

    public void setMaxActiveIssues(Integer maxActiveIssues) {
        this.maxActiveIssues = maxActiveIssues;
    }
}
