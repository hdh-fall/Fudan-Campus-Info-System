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
}
