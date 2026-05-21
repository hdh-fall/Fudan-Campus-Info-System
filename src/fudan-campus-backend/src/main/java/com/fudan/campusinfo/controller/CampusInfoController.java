package com.fudan.campusinfo.controller;

import com.fudan.campusinfo.entity.*;
import com.fudan.campusinfo.service.CSVImportService;
import com.fudan.campusinfo.service.CampusInfoService;
import com.fudan.campusinfo.service.NaturalLanguageQueryService;
import com.fudan.campusinfo.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CampusInfoController {

    @Autowired
    private CampusInfoService campusInfoService;

    @Autowired
    private NaturalLanguageQueryService nlQueryService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private CSVImportService csvImportService;

    // ==================== 校区接口 ====================
    @GetMapping("/campuses")
    public ResponseEntity<List<Campus>> getAllCampuses() {
        return ResponseEntity.ok(campusInfoService.getAllCampuses());
    }

    @GetMapping("/campuses/{id}")
    public ResponseEntity<Campus> getCampusById(@PathVariable Integer id) {
        Optional<Campus> campus = campusInfoService.getCampusById(id);
        return campus.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ==================== 建筑接口 ====================
    @GetMapping("/buildings/campus/{campusId}")
    public ResponseEntity<List<Building>> getBuildingsByCampus(@PathVariable Integer campusId) {
        return ResponseEntity.ok(campusInfoService.getBuildingsByCampusId(campusId));
    }

    @GetMapping("/buildings/search")
    public ResponseEntity<List<Building>> searchBuildings(@RequestParam String keyword) {
        return ResponseEntity.ok(campusInfoService.searchBuildings(keyword));
    }

    @GetMapping("/buildings/{id}")
    public ResponseEntity<Building> getBuildingById(@PathVariable Integer id) {
        Optional<Building> building = campusInfoService.getBuildingById(id);
        return building.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/buildings")
    public ResponseEntity<Building> createBuilding(@RequestBody Building building) {
        return ResponseEntity.ok(campusInfoService.createBuilding(building));
    }

    @PutMapping("/buildings/{id}")
    public ResponseEntity<Building> updateBuilding(@PathVariable Integer id, @RequestBody Building building) {
        return ResponseEntity.ok(campusInfoService.updateBuilding(id, building));
    }

    @DeleteMapping("/buildings/{id}")
    public ResponseEntity<Void> deleteBuilding(@PathVariable Integer id) {
        campusInfoService.deleteBuilding(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== 设施接口 ====================
    @GetMapping("/facilities/building/{buildingId}")
    public ResponseEntity<List<Facility>> getFacilitiesByBuilding(@PathVariable Integer buildingId) {
        return ResponseEntity.ok(campusInfoService.getFacilitiesByBuildingId(buildingId));
    }

    @GetMapping("/facilities/type/{type}")
    public ResponseEntity<List<Facility>> getFacilitiesByType(@PathVariable String type) {
        return ResponseEntity.ok(campusInfoService.getFacilitiesByType(type));
    }

    @GetMapping("/facilities/search")
    public ResponseEntity<List<Facility>> searchFacilities(@RequestParam String keyword) {
        return ResponseEntity.ok(campusInfoService.searchFacilities(keyword));
    }

    @GetMapping("/facilities/{id}")
    public ResponseEntity<Facility> getFacilityById(@PathVariable Integer id) {
        Optional<Facility> facility = campusInfoService.getFacilityById(id);
        return facility.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/facilities")
    public ResponseEntity<Facility> createFacility(@RequestBody Facility facility) {
        return ResponseEntity.ok(campusInfoService.createFacility(facility));
    }

    @PutMapping("/facilities/{id}")
    public ResponseEntity<Facility> updateFacility(@PathVariable Integer id, @RequestBody Facility facility) {
        return ResponseEntity.ok(campusInfoService.updateFacility(id, facility));
    }

    @DeleteMapping("/facilities/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable Integer id) {
        campusInfoService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== 课程接口 ====================
    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(campusInfoService.getAllCourses());
    }

    /**
     * 获取所有课程及其授课教师信息
     */
    @GetMapping("/courses-with-teachers")
    public ResponseEntity<List<Map<String, Object>>> getAllCoursesWithTeachers() {
        return ResponseEntity.ok(campusInfoService.getAllCoursesWithTeachers());
    }

    @GetMapping("/courses/department/{departmentId}")
    public ResponseEntity<List<Course>> getCoursesByDepartment(@PathVariable Integer departmentId) {
        return ResponseEntity.ok(campusInfoService.getCoursesByDepartmentId(departmentId));
    }

    @GetMapping("/courses/search")
    public ResponseEntity<List<Course>> searchCourses(@RequestParam String keyword) {
        return ResponseEntity.ok(campusInfoService.searchCourses(keyword));
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Integer id) {
        Optional<Course> course = campusInfoService.getCourseById(id);
        return course.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        return ResponseEntity.ok(campusInfoService.createCourse(course));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Integer id, @RequestBody Course course) {
        return ResponseEntity.ok(campusInfoService.updateCourse(id, course));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Integer id) {
        campusInfoService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== 教师接口 ====================
    @GetMapping("/teachers")
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        return ResponseEntity.ok(campusInfoService.getAllTeachers());
    }

    @GetMapping("/teachers/department/{departmentId}")
    public ResponseEntity<List<Teacher>> getTeachersByDepartment(@PathVariable Integer departmentId) {
        return ResponseEntity.ok(campusInfoService.getTeachersByDepartmentId(departmentId));
    }

    @GetMapping("/teachers/search")
    public ResponseEntity<List<Teacher>> searchTeachers(@RequestParam String keyword) {
        return ResponseEntity.ok(campusInfoService.searchTeachers(keyword));
    }

    @GetMapping("/teachers/{id}")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable Integer id) {
        Optional<Teacher> teacher = campusInfoService.getTeacherById(id);
        return teacher.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/teachers/{id}/courses")
    public ResponseEntity<List<Course>> getCoursesByTeacher(@PathVariable Integer id) {
        return ResponseEntity.ok(campusInfoService.getCoursesByTeacherId(id));
    }

    @PostMapping("/teachers")
    public ResponseEntity<Teacher> createTeacher(@RequestBody Teacher teacher) {
        return ResponseEntity.ok(campusInfoService.createTeacher(teacher));
    }

    @PutMapping("/teachers/{id}")
    public ResponseEntity<Teacher> updateTeacher(@PathVariable Integer id, @RequestBody Teacher teacher) {
        return ResponseEntity.ok(campusInfoService.updateTeacher(id, teacher));
    }

    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Integer id) {
        campusInfoService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== 活动接口 ====================
    @GetMapping("/events/upcoming")
    public ResponseEntity<List<Event>> getUpcomingEvents(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(campusInfoService.getUpcomingEvents(days));
    }

    @GetMapping("/events/campus/{campusId}")
    public ResponseEntity<List<Event>> getEventsByCampus(@PathVariable Integer campusId) {
        return ResponseEntity.ok(campusInfoService.getEventsByCampusId(campusId));
    }

    @GetMapping("/events/search")
    public ResponseEntity<List<Event>> searchEvents(@RequestParam String keyword) {
        return ResponseEntity.ok(campusInfoService.searchEvents(keyword));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Integer id) {
        Optional<Event> event = campusInfoService.getEventById(id);
        return event.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/events")
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        return ResponseEntity.ok(campusInfoService.createEvent(event));
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Integer id, @RequestBody Event event) {
        return ResponseEntity.ok(campusInfoService.updateEvent(id, event));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Integer id) {
        campusInfoService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== 用户接口 ====================
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(campusInfoService.getAllUsers());
    }

    @GetMapping("/users/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = campusInfoService.getUserByUsername(username);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        Optional<User> user = campusInfoService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(campusInfoService.createUser(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        campusInfoService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== 查询记录接口 ====================
    @PostMapping("/query-records")
    public ResponseEntity<QueryRecord> saveQueryRecord(@RequestBody QueryRecord record) {
        return ResponseEntity.ok(campusInfoService.saveQueryRecord(record));
    }

    @GetMapping("/query-records/user/{userId}")
    public ResponseEntity<List<QueryRecord>> getQueryHistory(@PathVariable Integer userId) {
        return ResponseEntity.ok(campusInfoService.getQueryHistoryByUserId(userId));
    }

    @GetMapping("/query-records/popular-categories")
    public ResponseEntity<List<Object[]>> getPopularCategories() {
        return ResponseEntity.ok(campusInfoService.getPopularCategories());
    }

    // ==================== 院系接口 ====================
    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(campusInfoService.getAllDepartments());
    }

    // ==================== 自然语言查询接口 ====================
    @PostMapping("/nl-query")
    public ResponseEntity<Map<String, Object>> naturalLanguageQuery(
            @RequestBody Map<String, String> request) {
        String question = request.get("question");
        Integer userId = request.get("userId") != null ? Integer.parseInt(request.get("userId")) : 1;
        
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Map<String, Object> result = nlQueryService.processNaturalLanguageQuery(question, userId);
        return ResponseEntity.ok(result);
    }

    // ==================== 统计分析接口 ====================
    
    /**
     * 统计不同校区的建筑数量
     */
    @GetMapping("/statistics/buildings-by-campus")
    public ResponseEntity<List<Map<String, Object>>> getBuildingsCountByCampus() {
        return ResponseEntity.ok(statisticsService.getBuildingsCountByCampus());
    }

    /**
     * 统计各院系课程数量
     */
    @GetMapping("/statistics/courses-by-department")
    public ResponseEntity<List<Map<String, Object>>> getCoursesCountByDepartment() {
        return ResponseEntity.ok(statisticsService.getCoursesCountByDepartment());
    }

    /**
     * 统计不同类型设施的数量
     */
    @GetMapping("/statistics/facilities-by-type")
    public ResponseEntity<List<Map<String, Object>>> getFacilitiesCountByType() {
        return ResponseEntity.ok(statisticsService.getFacilitiesCountByType());
    }

    /**
     * 统计最常被查询的类别
     */
    @GetMapping("/statistics/popular-categories")
    public ResponseEntity<List<Map<String, Object>>> getMostQueriedCategories() {
        return ResponseEntity.ok(statisticsService.getMostQueriedCategories());
    }

    /**
     * 热门课程统计
     */
    @GetMapping("/statistics/popular-courses")
    public ResponseEntity<List<Map<String, Object>>> getPopularCourses() {
        return ResponseEntity.ok(statisticsService.getPopularCourses());
    }

    /**
     * 热门活动统计
     */
    @GetMapping("/statistics/popular-events")
    public ResponseEntity<List<Map<String, Object>>> getPopularEvents() {
        return ResponseEntity.ok(statisticsService.getPopularEvents());
    }

    /**
     * 综合统计概览
     */
    @GetMapping("/statistics/overview")
    public ResponseEntity<Map<String, Object>> getOverviewStatistics() {
        return ResponseEntity.ok(statisticsService.getOverviewStatistics());
    }
    
    /**
     * 获取每日查询趋势（最近N天，默认7天）
     */
    @GetMapping("/statistics/daily-trend")
    public ResponseEntity<List<Map<String, Object>>> getDailyQueryTrend(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(statisticsService.getDailyQueryTrend(days));
    }
    
    /**
     * 获取活跃用户排行（默认前10名）
     */
    @GetMapping("/statistics/active-users")
    public ResponseEntity<List<Map<String, Object>>> getActiveUsers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statisticsService.getActiveUsers(limit));
    }
    
    /**
     * 获取校区设施热度统计
     */
    @GetMapping("/statistics/campus-facility-popularity")
    public ResponseEntity<List<Map<String, Object>>> getCampusFacilityPopularity() {
        return ResponseEntity.ok(statisticsService.getCampusFacilityPopularity());
    }

    // ==================== CSV批量导入接口 ====================
    
    /**
     * 从CSV文件批量导入建筑数据
     */
    @PostMapping("/import/buildings")
    public ResponseEntity<Map<String, Object>> importBuildings(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = csvImportService.importBuildings(file);
            result.put("success", true);
            result.put("message", "成功导入 " + count + " 条建筑数据");
            result.put("count", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 从CSV文件批量导入设施数据
     */
    @PostMapping("/import/facilities")
    public ResponseEntity<Map<String, Object>> importFacilities(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = csvImportService.importFacilities(file);
            result.put("success", true);
            result.put("message", "成功导入 " + count + " 条设施数据");
            result.put("count", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 从CSV文件批量导入课程数据
     */
    @PostMapping("/import/courses")
    public ResponseEntity<Map<String, Object>> importCourses(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = csvImportService.importCourses(file);
            result.put("success", true);
            result.put("message", "成功导入 " + count + " 条课程数据");
            result.put("count", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 从CSV文件批量导入教师数据
     */
    @PostMapping("/import/teachers")
    public ResponseEntity<Map<String, Object>> importTeachers(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = csvImportService.importTeachers(file);
            result.put("success", true);
            result.put("message", "成功导入 " + count + " 条教师数据");
            result.put("count", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 从CSV文件批量导入活动数据
     */
    @PostMapping("/import/events")
    public ResponseEntity<Map<String, Object>> importEvents(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = csvImportService.importEvents(file);
            result.put("success", true);
            result.put("message", "成功导入 " + count + " 条活动数据");
            result.put("count", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
