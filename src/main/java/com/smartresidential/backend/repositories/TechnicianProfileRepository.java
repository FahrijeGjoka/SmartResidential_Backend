package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.TechnicianProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, Long> {

    Optional<TechnicianProfile> findByUserId(Long userId);

    List<TechnicianProfile> findBySpecialization(String specialization);

    List<TechnicianProfile> findByIsAvailableTrue();
}