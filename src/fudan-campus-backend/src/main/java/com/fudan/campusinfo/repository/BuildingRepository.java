package com.fudan.campusinfo.repository;

import com.fudan.campusinfo.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Integer> {
    List<Building> findByCampus_CampusId(Integer campusId);
    List<Building> findByNameContaining(String keyword);
    
    @Query("SELECT b.campus.name, COUNT(b) FROM Building b GROUP BY b.campus.name")
    List<Object[]> countBuildingsByCampus();
    
    /**
     * 获取校区设施统计（包括建筑数量和设施数量）
     */
    @Query("SELECT c.campusId, c.name, COUNT(DISTINCT b.buildingId), COUNT(DISTINCT f.facilityId) " +
           "FROM Campus c " +
           "LEFT JOIN Building b ON c.campusId = b.campus.campusId " +
           "LEFT JOIN Facility f ON b.buildingId = f.building.buildingId " +
           "GROUP BY c.campusId, c.name " +
           "ORDER BY COUNT(DISTINCT f.facilityId) DESC")
    List<Object[]> findCampusFacilityStats();
}
