package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.BuildingAnnouncement;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface BuildingAnnouncementRepository extends BaseRepository<BuildingAnnouncement, Long> {
    List<BuildingAnnouncement> findByBuildingId(Long buildingId);
    List<BuildingAnnouncement> findByCreatedById(Long userId);
    List<BuildingAnnouncement> findTop10ByBuildingIdOrderByCreatedAtDesc(Long buildingId);
    @Query("""
        SELECT ba
        FROM BuildingAnnouncement ba
        WHERE ba.building.id = :buildingId
        ORDER BY ba.createdAt DESC
    """)
    List<BuildingAnnouncement> findAllByBuildingOrderByNewest(@Param("buildingId") Long buildingId);
    @Query("""
        SELECT ba
        FROM BuildingAnnouncement ba
        WHERE ba.createdBy.id = :userId
        ORDER BY ba.createdAt DESC
    """)
    List<BuildingAnnouncement> findAllByCreatorOrderByNewest(@Param("userId") Long userId);
}

