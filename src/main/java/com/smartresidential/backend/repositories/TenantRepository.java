package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByIdentifier(String identifier);

    Optional<Tenant> findBySchemaName(String schemaName);

    boolean existsByIdentifier(String identifier);

    boolean existsBySchemaName(String schemaName);
}
