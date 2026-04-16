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

    public TechnicianProfile() {
    }

    @PrePersist
    public void prePersist() {
        if (this.isAvailable == null) {
            this.isAvailable = true;
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
}
