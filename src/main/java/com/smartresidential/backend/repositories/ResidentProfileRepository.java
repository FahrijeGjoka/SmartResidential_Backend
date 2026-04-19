package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.ResidentProfile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface ResidentProfileRepository extends BaseRepository<ResidentProfile, Long> {
    Optional<ResidentProfile> findByUserId(Long userId);
    Optional<ResidentProfile> findByApartmentId(Long apartmentId);
    List<ResidentProfile> findByApartmentBuildingId(Long buildingId);
    @Query("""
        SELECT rp
        FROM ResidentProfile rp
        JOIN rp.apartment a
        JOIN a.building b
        WHERE b.id = :buildingId
    """)
    List<ResidentProfile> findResidentsByBuilding(@Param("buildingId") Long buildingId);
    @Query("""
        SELECT rp
        FROM ResidentProfile rp
        WHERE rp.user.id = :userId
          AND rp.apartment.building.id = :buildingId
    """)
    Optional<ResidentProfile> findByUserAndBuilding(@Param("userId") Long userId,
                                                    @Param("buildingId") Long buildingId);
}
