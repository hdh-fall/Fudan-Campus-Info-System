package com.fudan.campusinfo.service;

import com.fudan.campusinfo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 统计分析服务
 * 提供各类数据统计功能
 */
@Service
public class StatisticsService {

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private QueryRecordRepository queryRecordRepository;

    /**
     * 统计不同校区的建筑数量
     */
    public List<Map<String, Object>> getBuildingsCountByCampus() {
        List<Object[]> results = buildingRepository.countBuildingsByCampus();
        List<Map<String, Object>> statistics = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("campusName", row[0]);
            stat.put("buildingCount", row[1]);
            statistics.add(stat);
        }
        
        return statistics;
    }

    /**
     * 统计各院系课程数量
     */
    public List<Map<String, Object>> getCoursesCountByDepartment() {
        List<Object[]> results = courseRepository.countCoursesByDepartment();
        List<Map<String, Object>> statistics = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("departmentName", row[0]);
            stat.put("courseCount", row[1]);
            statistics.add(stat);
        }
        
        return statistics;
    }

    /**
     * 统计不同类型校园设施的数量
     */
    public List<Map<String, Object>> getFacilitiesCountByType() {
        List<Object[]> results = facilityRepository.countFacilitiesByType();
        List<Map<String, Object>> statistics = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("facilityType", row[0]);
            stat.put("facilityCount", row[1]);
            statistics.add(stat);
        }
        
        return statistics;
    }

    /**
     * 统计最常被查询的校园信息类别
     */
    public List<Map<String, Object>> getMostQueriedCategories() {
        List<Object[]> results = queryRecordRepository.countQueriesByCategory();
        List<Map<String, Object>> statistics = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("category", row[0]);
            stat.put("queryCount", row[1]);
            statistics.add(stat);
        }
        
        return statistics;
    }

    /**
     * 热门课程统计（根据查询记录）
     */
    public List<Map<String, Object>> getPopularCourses() {
        // 这里简化处理，实际应该分析查询记录中的课程提及频率
        List<Map<String, Object>> statistics = new ArrayList<>();
        
        // 示例：返回查询次数最多的前10个课程类别
        List<Object[]> results = queryRecordRepository.findPopularQueryTopics();
        
        for (Object[] row : results) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("topic", row[0]);
            stat.put("mentionCount", row[1]);
            statistics.add(stat);
        }
        
        return statistics;
    }

    /**
     * 热门活动统计
     */
    public List<Map<String, Object>> getPopularEvents() {
        List<Map<String, Object>> statistics = new ArrayList<>();
        
        // 统计各类型活动的数量
        List<Object[]> results = eventRepository.countEventsByCategory();
        
        for (Object[] row : results) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("category", row[0]);
            stat.put("eventCount", row[1]);
            statistics.add(stat);
        }
        
        return statistics;
    }

    /**
     * 综合统计概览
     */
    public Map<String, Object> getOverviewStatistics() {
        Map<String, Object> overview = new HashMap<>();
        
        overview.put("totalCampuses", campusRepository.count());
        overview.put("totalBuildings", buildingRepository.count());
        overview.put("totalFacilities", facilityRepository.count());
        overview.put("totalCourses", courseRepository.count());
        overview.put("totalTeachers", teacherRepository.count());
        overview.put("totalEvents", eventRepository.count());
        overview.put("totalQueries", queryRecordRepository.count());
        
        return overview;
    }
    
    /**
     * 获取每日查询趋势（最近N天）
     */
    public List<Map<String, Object>> getDailyQueryTrend(int days) {
        List<Object[]> results = queryRecordRepository.findDailyQueryTrend(days);
        List<Map<String, Object>> trend = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0].toString());
            item.put("count", row[1]);
            item.put("uniqueUsers", row[2]);
            trend.add(item);
        }
        
        return trend;
    }
    
    /**
     * 获取活跃用户排行
     */
    public List<Map<String, Object>> getActiveUsers(int limit) {
        List<Object[]> results = queryRecordRepository.findActiveUsers(limit);
        List<Map<String, Object>> users = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            user.put("userId", row[0]);
            user.put("username", row[1]);
            user.put("name", row[2]);
            user.put("totalQueries", row[3]);
            user.put("categoriesExplored", row[4]);
            users.add(user);
        }
        
        return users;
    }
    
    /**
     * 获取校区设施热度统计
     */
    public List<Map<String, Object>> getCampusFacilityPopularity() {
        List<Object[]> results = buildingRepository.findCampusFacilityStats();
        List<Map<String, Object>> stats = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("campusId", row[0]);
            item.put("campusName", row[1]);
            item.put("buildingCount", row[2]);
            item.put("facilityCount", row[3]);
            stats.add(item);
        }
        
        return stats;
    }
}
