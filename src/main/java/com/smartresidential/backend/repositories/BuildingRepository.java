package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.Building;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface BuildingRepository extends BaseRepository<Building, Long> {
    Optional<Building> findByName(String name);
    boolean existsByName(String name);
    @Query("""
        SELECT b
        FROM Building b
        WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Building> searchByName(@Param("name") String name);
    @Query("""
        SELECT b
        FROM Building b
        WHERE LOWER(b.address) LIKE LOWER(CONCAT('%', :address, '%'))
    """)
    List<Building> searchByAddress(@Param("address") String address);
}
