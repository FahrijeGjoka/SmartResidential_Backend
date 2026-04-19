package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.Apartment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface ApartmentRepository extends BaseRepository<Apartment, Long> {
    List<Apartment> findByBuildingId(Long buildingId);
    Optional<Apartment> findByBuildingIdAndUnitNumber(Long buildingId, String unitNumber);
    boolean existsByBuildingIdAndUnitNumber(Long buildingId, String unitNumber);
    @Query("""
        SELECT a
        FROM Apartment a
        WHERE a.building.id = :buildingId
          AND a.floor = :floor
    """)
    List<Apartment> findByBuildingAndFloor(@Param("buildingId") Long buildingId,
                                           @Param("floor") Integer floor);
    @Query("""
        SELECT a
        FROM Apartment a
        WHERE LOWER(a.unitNumber) LIKE LOWER(CONCAT('%', :unitNumber, '%'))
    """)
    List<Apartment> searchByUnitNumber(@Param("unitNumber") String unitNumber);
}

