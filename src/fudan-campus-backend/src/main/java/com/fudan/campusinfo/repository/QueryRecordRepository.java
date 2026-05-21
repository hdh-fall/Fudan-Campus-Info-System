package com.fudan.campusinfo.repository;

import com.fudan.campusinfo.entity.QueryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QueryRecordRepository extends JpaRepository<QueryRecord, Integer> {
    List<QueryRecord> findByUser_UserIdOrderByQueryTimeDesc(Integer userId);
    
    @Query("SELECT qr.category, COUNT(qr) FROM QueryRecord qr GROUP BY qr.category ORDER BY COUNT(qr) DESC")
    List<Object[]> countByCategory();
    
    // 别名方法，用于StatisticsService
    default List<Object[]> countQueriesByCategory() {
        return countByCategory();
    }
    
    @Query("SELECT SUBSTRING_INDEX(qr.question, ' ', 1) as topic, COUNT(qr) as cnt FROM QueryRecord qr GROUP BY topic ORDER BY cnt DESC LIMIT 10")
    List<Object[]> findPopularQueryTopics();
}
