package com.fudan.campusinfo.repository;

import com.fudan.campusinfo.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Integer> {
    List<Facility> findByBuilding_BuildingId(Integer buildingId);
    List<Facility> findByType(String type);
    List<Facility> findByNameContaining(String keyword);
    
    @Query("SELECT f.type, COUNT(f) FROM Facility f GROUP BY f.type")
    List<Object[]> countFacilitiesByType();
}
