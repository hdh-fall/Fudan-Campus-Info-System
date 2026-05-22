package com.fudan.campusinfo.repository;

import com.fudan.campusinfo.entity.QueryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    /**
     * 获取每日查询趋势（最近N天）
     */
    @Query(value = "SELECT DATE(qr.query_time), COUNT(qr.record_id), COUNT(DISTINCT qr.user_id) " +
           "FROM query_record qr " +
           "WHERE qr.query_time >= DATE_SUB(CURRENT_DATE(), INTERVAL :days DAY) " +
           "GROUP BY DATE(qr.query_time) " +
           "ORDER BY DATE(qr.query_time) DESC", nativeQuery = true)
    List<Object[]> findDailyQueryTrend(@Param("days") int days);
    
    /**
     * 获取活跃用户排行
     */
    @Query("SELECT qr.user.userId, qr.user.username, qr.user.name, " +
           "COUNT(qr), COUNT(DISTINCT qr.category) " +
           "FROM QueryRecord qr " +
           "GROUP BY qr.user.userId, qr.user.username, qr.user.name " +
           "ORDER BY COUNT(qr) DESC")
    List<Object[]> findActiveUsers(@Param("limit") int limit);
}
