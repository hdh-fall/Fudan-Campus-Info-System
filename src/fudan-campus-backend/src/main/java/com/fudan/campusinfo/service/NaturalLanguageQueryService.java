package com.fudan.campusinfo.service;

import com.fudan.campusinfo.entity.*;
import com.fudan.campusinfo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自然语言查询服务
 * 将用户的自然语言问题转换为数据库查询
 */
@Service
public class NaturalLanguageQueryService {

    @Autowired
    private CampusInfoService campusInfoService;

    @Autowired
    private QueryRecordRepository queryRecordRepository;

    /**
     * 处理自然语言查询
     * @param question 用户问题
     * @param userId 用户ID
     * @return 查询结果
     */
    public Map<String, Object> processNaturalLanguageQuery(String question, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        
        // 解析问题类型
        QueryType queryType = parseQueryType(question);
        result.put("queryType", queryType.getDescription());
        
        // 执行对应的查询
        List<Map<String, Object>> data = executeQuery(question, queryType);
        result.put("data", data);
        result.put("count", data.size());
        
        // 保存查询记录
        saveQueryRecord(userId, question, queryType, data);
        
        return result;
    }

    /**
     * 解析问题类型
     */
    private QueryType parseQueryType(String question) {
        String lowerQuestion = question.toLowerCase();
        
        // 食堂相关
        if (lowerQuestion.contains("食堂") || lowerQuestion.contains("吃饭") || lowerQuestion.contains("餐厅")) {
            return QueryType.FACILITY_CANTEEN;
        }
        
        // 咖啡厅相关
        if (lowerQuestion.contains("咖啡") || lowerQuestion.contains("cafe") || lowerQuestion.contains("饮品")) {
            return QueryType.FACILITY_CAFE;
        }
        
        // 图书馆/自习室相关
        if (lowerQuestion.contains("图书馆") || lowerQuestion.contains("自习") || lowerQuestion.contains("阅览室")) {
            return QueryType.FACILITY_LIBRARY;
        }
        
        // 建筑相关
        if (lowerQuestion.contains("楼") || lowerQuestion.contains("建筑") || lowerQuestion.contains("教学楼")) {
            return QueryType.BUILDING;
        }
        
        // 校区相关
        if (lowerQuestion.contains("校区")) {
            return QueryType.CAMPUS;
        }
        
        // 课程相关
        if (lowerQuestion.contains("课程") || lowerQuestion.contains("课")) {
            return QueryType.COURSE;
        }
        
        // 教师相关
        if (lowerQuestion.contains("老师") || lowerQuestion.contains("教师") || lowerQuestion.contains("教授")) {
            return QueryType.TEACHER;
        }
        
        // 活动相关
        if (lowerQuestion.contains("活动") || lowerQuestion.contains("讲座") || lowerQuestion.contains("论坛") || lowerQuestion.contains("晚会")) {
            return QueryType.EVENT;
        }
        
        // 默认返回通用搜索
        return QueryType.GENERAL_SEARCH;
    }

    /**
     * 执行查询
     */
    private List<Map<String, Object>> executeQuery(String question, QueryType queryType) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        switch (queryType) {
            case FACILITY_CANTEEN:
                results = queryFacilitiesByType("食堂");
                break;
            case FACILITY_CAFE:
                results = queryFacilitiesByType("咖啡厅");
                break;
            case FACILITY_LIBRARY:
                // 查询图书馆和自习室
                List<Map<String, Object>> libraries = queryFacilitiesByType("阅览室");
                List<Map<String, Object>> studyRooms = queryFacilitiesByType("自习室");
                results.addAll(libraries);
                results.addAll(studyRooms);
                break;
            case BUILDING:
                results = queryBuildings(question);
                break;
            case CAMPUS:
                results = queryCampuses();
                break;
            case COURSE:
                results = queryCourses(question);
                break;
            case TEACHER:
                results = queryTeachers(question);
                break;
            case EVENT:
                results = queryEvents(question);
                break;
            case GENERAL_SEARCH:
            default:
                results = generalSearch(question);
                break;
        }
        
        return results;
    }

    /**
     * 查询指定类型的设施
     */
    private List<Map<String, Object>> queryFacilitiesByType(String type) {
        List<Facility> facilities = campusInfoService.getFacilitiesByType(type);
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Facility facility : facilities) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", facility.getName());
            item.put("type", facility.getType());
            item.put("location", facility.getLocationDesc());
            item.put("building", facility.getBuilding() != null ? facility.getBuilding().getName() : "未知");
            item.put("openTime", facility.getOpenTime());
            item.put("capacity", facility.getCapacity());
            item.put("description", facility.getDescription());
            results.add(item);
        }
        
        return results;
    }

    /**
     * 查询建筑
     */
    private List<Map<String, Object>> queryBuildings(String question) {
        // 提取校区名称
        String campusName = extractCampusName(question);
        
        List<Building> buildings;
        if (campusName != null) {
            // 先找到校区ID
            List<Campus> campuses = campusInfoService.getAllCampuses();
            Integer campusId = null;
            for (Campus campus : campuses) {
                if (campus.getName().contains(campusName)) {
                    campusId = campus.getCampusId();
                    break;
                }
            }
            
            if (campusId != null) {
                buildings = campusInfoService.getBuildingsByCampusId(campusId);
            } else {
                buildings = campusInfoService.searchBuildings(campusName);
            }
        } else {
            // 提取建筑关键词
            String keyword = extractKeyword(question, new String[]{"楼", "建筑", "教学楼", "宿舍", "馆"});
            buildings = campusInfoService.searchBuildings(keyword != null ? keyword : "");
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        for (Building building : buildings) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", building.getName());
            item.put("type", building.getType());
            item.put("campus", building.getCampus() != null ? building.getCampus().getName() : "未知");
            item.put("floors", building.getFloors());
            item.put("openTime", building.getOpenTime());
            item.put("closeTime", building.getCloseTime());
            item.put("description", building.getDescription());
            results.add(item);
        }
        
        return results;
    }

    /**
     * 查询校区
     */
    private List<Map<String, Object>> queryCampuses() {
        List<Campus> campuses = campusInfoService.getAllCampuses();
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Campus campus : campuses) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", campus.getName());
            item.put("address", campus.getAddress());
            item.put("phone", campus.getContactPhone());
            item.put("description", campus.getDescription());
            results.add(item);
        }
        
        return results;
    }

    /**
     * 查询课程
     */
    private List<Map<String, Object>> queryCourses(String question) {
        // 提取课程名称或教师名称
        String keyword = extractKeyword(question, new String[]{"课程", "课"});
        
        List<Course> courses;
        if (keyword != null && !keyword.isEmpty()) {
            courses = campusInfoService.searchCourses(keyword);
        } else {
            courses = campusInfoService.getAllCourses();
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        for (Course course : courses) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", course.getName());
            item.put("department", course.getDepartment() != null ? course.getDepartment().getName() : "未知");
            item.put("credits", course.getCredits());
            item.put("semester", course.getSemesterOffered());
            item.put("description", course.getDescription());
            
            // 获取授课教师
            List<Teacher> teachers = campusInfoService.getCoursesByTeacherId(1).stream()
                .filter(c -> c.getCourseId().equals(course.getCourseId()))
                .flatMap(c -> campusInfoService.getAllTeachers().stream())
                .toList();
            
            results.add(item);
        }
        
        return results;
    }

    /**
     * 查询教师
     */
    private List<Map<String, Object>> queryTeachers(String question) {
        String keyword = extractKeyword(question, new String[]{"老师", "教师", "教授"});
        
        List<Teacher> teachers;
        if (keyword != null && !keyword.isEmpty()) {
            teachers = campusInfoService.searchTeachers(keyword);
        } else {
            teachers = campusInfoService.getAllTeachers();
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        for (Teacher teacher : teachers) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", teacher.getName());
            item.put("department", teacher.getDepartment() != null ? teacher.getDepartment().getName() : "未知");
            item.put("title", teacher.getTitle());
            item.put("email", teacher.getEmail());
            item.put("phone", teacher.getPhone());
            results.add(item);
        }
        
        return results;
    }

    /**
     * 查询活动
     */
    private List<Map<String, Object>> queryEvents(String question) {
        // 判断是否查询近期活动
        boolean upcoming = question.contains("近期") || question.contains("最近") || question.contains("下周");
        
        List<Event> events;
        if (upcoming) {
            events = campusInfoService.getUpcomingEvents(30);
        } else {
            String keyword = extractKeyword(question, new String[]{"活动", "讲座", "论坛", "晚会"});
            events = campusInfoService.searchEvents(keyword != null ? keyword : "");
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        for (Event event : events) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", event.getName());
            item.put("time", event.getEventTime());
            item.put("location", event.getLocationDesc());
            item.put("organizer", event.getOrganizer());
            item.put("category", event.getCategory());
            item.put("description", event.getDescription());
            results.add(item);
        }
        
        return results;
    }

    /**
     * 通用搜索
     */
    private List<Map<String, Object>> generalSearch(String keyword) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // 搜索建筑
        List<Building> buildings = campusInfoService.searchBuildings(keyword);
        for (Building b : buildings) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "建筑");
            item.put("name", b.getName());
            item.put("detail", b.getCampus() != null ? b.getCampus().getName() : "");
            results.add(item);
        }
        
        // 搜索设施
        List<Facility> facilities = campusInfoService.searchFacilities(keyword);
        for (Facility f : facilities) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "设施");
            item.put("name", f.getName());
            item.put("detail", f.getType());
            results.add(item);
        }
        
        // 搜索课程
        List<Course> courses = campusInfoService.searchCourses(keyword);
        for (Course c : courses) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "课程");
            item.put("name", c.getName());
            item.put("detail", c.getDepartment() != null ? c.getDepartment().getName() : "");
            results.add(item);
        }
        
        return results;
    }

    /**
     * 提取校区名称
     */
    private String extractCampusName(String question) {
        String[] campuses = {"邯郸", "枫林", "张江", "江湾"};
        for (String campus : campuses) {
            if (question.contains(campus)) {
                return campus;
            }
        }
        return null;
    }

    /**
     * 提取关键词
     */
    private String extractKeyword(String question, String[] removeWords) {
        String result = question;
        for (String word : removeWords) {
            result = result.replace(word, "");
        }
        result = result.replaceAll("[的什么哪些有]", "").trim();
        return result.isEmpty() ? null : result;
    }

    /**
     * 保存查询记录
     */
    private void saveQueryRecord(Integer userId, String question, QueryType queryType, List<Map<String, Object>> data) {
        try {
            QueryRecord record = new QueryRecord();
            
            // 获取用户对象
            User user = new User();
            user.setUserId(userId);
            record.setUser(user);
            
            record.setQuestion(question);
            record.setQueryTime(LocalDateTime.now());
            record.setCategory(queryType.name());
            record.setResultSummary(data.size() + "条结果");
            record.setUsedNl2sql(true);
            
            queryRecordRepository.save(record);
        } catch (Exception e) {
            // 记录失败不影响查询结果
            System.err.println("保存查询记录失败: " + e.getMessage());
        }
    }

    /**
     * 查询类型枚举
     */
    enum QueryType {
        FACILITY_CANTEEN("食堂查询"),
        FACILITY_CAFE("咖啡厅查询"),
        FACILITY_LIBRARY("图书馆/自习室查询"),
        BUILDING("建筑查询"),
        CAMPUS("校区查询"),
        COURSE("课程查询"),
        TEACHER("教师查询"),
        EVENT("活动查询"),
        GENERAL_SEARCH("通用搜索");

        private final String description;

        QueryType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
