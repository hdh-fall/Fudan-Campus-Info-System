package com.fudan.campusinfo.repository;

import com.fudan.campusinfo.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByEventTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Event> findByCampus_CampusId(Integer campusId);
    List<Event> findByNameContainingOrOrganizerContaining(String nameKeyword, String organizerKeyword);
    
    @Query("SELECT e.category, COUNT(e) FROM Event e GROUP BY e.category")
    List<Object[]> countEventsByCategory();
}
