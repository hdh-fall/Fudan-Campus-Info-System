package com.fudan.campusinfo.service;

import com.fudan.campusinfo.entity.*;
import com.fudan.campusinfo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CSVImportService {

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
    private DepartmentRepository departmentRepository;

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private CourseTeacherRepository courseTeacherRepository;

    /**
     * 从 CSV文件批量导入建筑数据
     * CSV格式: name,type,campus_id,floors,open_time,close_time,description
     */
    @Transactional
    public int importBuildings(MultipartFile file) throws Exception {
        List<Building> buildings = new ArrayList<>();
        int skipCount = 0;
            
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
                
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // 跳过标题行
                }
                    
                String[] fields = line.split(",", -1);
                if (fields.length >= 3) {
                    String buildingName = fields[0].trim();
                    
                    // 检查是否已存在同名建筑
                    List<Building> existing = buildingRepository.findByNameContaining(buildingName);
                    boolean exists = false;
                    for (Building b : existing) {
                        if (b.getName().equals(buildingName)) {
                            exists = true;
                            break;
                        }
                    }
                    
                    if (exists) {
                        skipCount++;
                        continue; // 跳过重复的建筑
                    }
                    
                    Building building = new Building();
                    building.setName(buildingName);
                    building.setType(fields[1].trim());
                        
                    if (!fields[2].trim().isEmpty()) {
                        Integer campusId = Integer.parseInt(fields[2].trim());
                        Campus campus = campusRepository.findById(campusId)
                            .orElseThrow(() -> new RuntimeException("校区不存在: " + campusId));
                        building.setCampus(campus);
                    }
                        
                    if (fields.length > 3 && !fields[3].trim().isEmpty()) {
                        building.setFloors(Integer.parseInt(fields[3].trim()));
                    }
                        
                    // 处理开放时间 - 支持多种格式
                    if (fields.length > 4 && !fields[4].trim().isEmpty()) {
                        String openTimeStr = fields[4].trim();
                        try {
                            // 如果是 HH:mm 格式，补充为 HH:mm:ss
                            if (openTimeStr.matches("\\d{2}:\\d{2}")) {
                                openTimeStr += ":00";
                            }
                            building.setOpenTime(java.sql.Time.valueOf(openTimeStr));
                        } catch (IllegalArgumentException e) {
                            System.err.println("建筑 '" + buildingName + "' 的开放时间格式错误: " + openTimeStr);
                        }
                    }
                        
                    // 处理关闭时间 - 支持多种格式
                    if (fields.length > 5 && !fields[5].trim().isEmpty()) {
                        String closeTimeStr = fields[5].trim();
                        try {
                            // 如果是 HH:mm 格式，补充为 HH:mm:ss
                            if (closeTimeStr.matches("\\d{2}:\\d{2}")) {
                                closeTimeStr += ":00";
                            }
                            building.setCloseTime(java.sql.Time.valueOf(closeTimeStr));
                        } catch (IllegalArgumentException e) {
                            System.err.println("建筑 '" + buildingName + "' 的关闭时间格式错误: " + closeTimeStr);
                        }
                    }
                        
                    if (fields.length > 6) {
                        building.setDescription(fields[6].trim());
                    }
                        
                    buildings.add(building);
                }
            }
        }
            
        buildingRepository.saveAll(buildings);
        int importedCount = buildings.size();
        System.out.println("建筑导入完成: 成功导入 " + importedCount + " 条，跳过重复 " + skipCount + " 条");
        return importedCount;
    }

    /**
     * 从 CSV文件批量导入设施数据
     * CSV格式: name,type,building_id,open_time,location_desc,capacity,contact,description
     */
    @Transactional
    public int importFacilities(MultipartFile file) throws Exception {
        List<Facility> facilities = new ArrayList<>();
        int skipCount = 0;
            
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
                
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                    
                String[] fields = line.split(",", -1);
                if (fields.length >= 3) {
                    String facilityName = fields[0].trim();
                    
                    // 检查是否已存在同名设施
                    List<Facility> existing = facilityRepository.findByNameContaining(facilityName);
                    boolean exists = false;
                    for (Facility f : existing) {
                        if (f.getName().equals(facilityName)) {
                            exists = true;
                            break;
                        }
                    }
                    
                    if (exists) {
                        skipCount++;
                        continue; // 跳过重复的设施
                    }
                    
                    Facility facility = new Facility();
                    facility.setName(facilityName);
                    facility.setType(fields[1].trim());
                        
                    if (!fields[2].trim().isEmpty()) {
                        Integer buildingId = Integer.parseInt(fields[2].trim());
                        Building building = buildingRepository.findById(buildingId)
                            .orElseThrow(() -> new RuntimeException("建筑不存在: " + buildingId));
                        facility.setBuilding(building);
                    }
                        
                    if (fields.length > 3) {
                        facility.setOpenTime(fields[3].trim());
                    }
                        
                    if (fields.length > 4) {
                        facility.setLocationDesc(fields[4].trim());
                    }
                        
                    if (fields.length > 5 && !fields[5].trim().isEmpty()) {
                        facility.setCapacity(Integer.parseInt(fields[5].trim()));
                    }
                        
                    if (fields.length > 6) {
                        facility.setContact(fields[6].trim());
                    }
                        
                    if (fields.length > 7) {
                        facility.setDescription(fields[7].trim());
                    }
                        
                    facilities.add(facility);
                }
            }
        }
            
        facilityRepository.saveAll(facilities);
        int importedCount = facilities.size();
        System.out.println("设施导入完成: 成功导入 " + importedCount + " 条，跳过重复 " + skipCount + " 条");
        return importedCount;
    }

    /**
     * 从CSV文件批量导入课程数据
     * CSV格式: name,department_id,credits,semester_offered,description
     */
    @Transactional
    public int importCourses(MultipartFile file) throws Exception {
        List<Course> courses = new ArrayList<>();
        int skipCount = 0;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] fields = line.split(",", -1);
                if (fields.length >= 1) {
                    String courseName = fields[0].trim();
                    
                    // 检查是否已存在同名课程
                    List<Course> existing = courseRepository.findByNameContaining(courseName);
                    boolean exists = false;
                    for (Course c : existing) {
                        if (c.getName().equals(courseName)) {
                            exists = true;
                            break;
                        }
                    }
                    
                    if (exists) {
                        skipCount++;
                        continue; // 跳过重复的课程
                    }
                    
                    Course course = new Course();
                    course.setName(courseName);
                    
                    if (fields.length > 1 && !fields[1].trim().isEmpty()) {
                        Integer deptId = Integer.parseInt(fields[1].trim());
                        Department dept = departmentRepository.findById(deptId)
                            .orElseThrow(() -> new RuntimeException("院系不存在: " + deptId));
                        course.setDepartment(dept);
                    }
                    
                    if (fields.length > 2 && !fields[2].trim().isEmpty()) {
                        course.setCredits(new java.math.BigDecimal(fields[2].trim()));
                    }
                    
                    if (fields.length > 3) {
                        course.setSemesterOffered(fields[3].trim());
                    }
                    
                    if (fields.length > 4) {
                        course.setDescription(fields[4].trim());
                    }
                    
                    courses.add(course);
                }
            }
        }
        
        courseRepository.saveAll(courses);
        int importedCount = courses.size();
        System.out.println("课程导入完成: 成功导入 " + importedCount + " 条，跳过重复 " + skipCount + " 条");
        return importedCount;
    }

    /**
     * 从CSV文件批量导入教师数据
     * CSV格式: name,department_id,title,email,phone
     */
    @Transactional
    public int importTeachers(MultipartFile file) throws Exception {
        List<Teacher> teachers = new ArrayList<>();
        int skipCount = 0;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] fields = line.split(",", -1);
                if (fields.length >= 1) {
                    String teacherName = fields[0].trim();
                    
                    // 检查是否已存在同名教师
                    List<Teacher> existing = teacherRepository.findByNameContaining(teacherName);
                    boolean exists = false;
                    for (Teacher t : existing) {
                        if (t.getName().equals(teacherName)) {
                            exists = true;
                            break;
                        }
                    }
                    
                    if (exists) {
                        skipCount++;
                        continue; // 跳过重复的教师
                    }
                    
                    Teacher teacher = new Teacher();
                    teacher.setName(teacherName);
                    
                    if (fields.length > 1 && !fields[1].trim().isEmpty()) {
                        Integer deptId = Integer.parseInt(fields[1].trim());
                        Department dept = departmentRepository.findById(deptId)
                            .orElseThrow(() -> new RuntimeException("院系不存在: " + deptId));
                        teacher.setDepartment(dept);
                    }
                    
                    if (fields.length > 2) {
                        teacher.setTitle(fields[2].trim());
                    }
                    
                    if (fields.length > 3) {
                        teacher.setEmail(fields[3].trim());
                    }
                    
                    if (fields.length > 4) {
                        teacher.setPhone(fields[4].trim());
                    }
                    
                    teachers.add(teacher);
                }
            }
        }
        
        teacherRepository.saveAll(teachers);
        int importedCount = teachers.size();
        System.out.println("教师导入完成: 成功导入 " + importedCount + " 条，跳过重复 " + skipCount + " 条");
        return importedCount;
    }

    /**
     * 从 CSV文件批量导入活动数据
     * CSV格式: name,event_time,location_desc,campus_id,building_id,organizer,category,description
     */
    @Transactional
    public int importEvents(MultipartFile file) throws Exception {
        List<Event> events = new ArrayList<>();
        int skipCount = 0;
            
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
                
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                    
                String[] fields = line.split(",", -1);
                if (fields.length >= 2) {
                    String eventName = fields[0].trim();
                    
                    // 检查是否已存在同名活动
                    List<Event> existing = eventRepository.findByNameContaining(eventName);
                    boolean exists = false;
                    for (Event e : existing) {
                        if (e.getName().equals(eventName)) {
                            exists = true;
                            break;
                        }
                    }
                    
                    if (exists) {
                        skipCount++;
                        continue; // 跳过重复的活动
                    }
                    
                    Event event = new Event();
                    event.setName(eventName);
                        
                    // 处理活动时间 - 支持多种日期时间格式
                    if (!fields[1].trim().isEmpty()) {
                        try {
                            String dateTimeStr = fields[1].trim();
                            // 尝试解析 LocalDateTime
                            java.time.LocalDateTime eventTime;
                            if (dateTimeStr.contains("T")) {
                                // ISO 格式: yyyy-MM-ddTHH:mm:ss
                                eventTime = java.time.LocalDateTime.parse(dateTimeStr);
                            } else if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                                // 标准格式: yyyy-MM-dd HH:mm:ss
                                eventTime = java.time.LocalDateTime.parse(dateTimeStr, 
                                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            } else if (dateTimeStr.matches("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}")) {
                                // 斜杠格式: yyyy/MM/dd HH:mm
                                eventTime = java.time.LocalDateTime.parse(dateTimeStr + ":00",
                                    java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
                            } else {
                                // 默认尝试直接解析
                                eventTime = java.time.LocalDateTime.parse(dateTimeStr);
                            }
                            event.setEventTime(eventTime);
                        } catch (Exception e) {
                            System.err.println("活动 '" + eventName + "' 的时间格式错误: " + fields[1].trim());
                            continue; // 跳过时间格式错误的记录
                        }
                    }
                        
                    if (fields.length > 2) {
                        event.setLocationDesc(fields[2].trim());
                    }
                        
                    if (fields.length > 3 && !fields[3].trim().isEmpty()) {
                        Integer campusId = Integer.parseInt(fields[3].trim());
                        Campus campus = campusRepository.findById(campusId)
                            .orElse(null);
                        event.setCampus(campus);
                    }
                        
                    if (fields.length > 4 && !fields[4].trim().isEmpty()) {
                        Integer buildingId = Integer.parseInt(fields[4].trim());
                        Building building = buildingRepository.findById(buildingId)
                            .orElse(null);
                        event.setBuilding(building);
                    }
                        
                    if (fields.length > 5) {
                        event.setOrganizer(fields[5].trim());
                    }
                        
                    if (fields.length > 6) {
                        event.setCategory(fields[6].trim());
                    }
                        
                    if (fields.length > 7) {
                        event.setDescription(fields[7].trim());
                    }
                        
                    events.add(event);
                }
            }
        }
            
        eventRepository.saveAll(events);
        int importedCount = events.size();
        System.out.println("活动导入完成: 成功导入 " + importedCount + " 条，跳过重复 " + skipCount + " 条");
        return importedCount;
    }

    /**
     * 从 CSV文件批量导入课程-教师关联数据
     * CSV格式: course_id,teacher_id,semester,role,remarks
     */
    @Transactional
    public int importCourseTeachers(MultipartFile file) throws Exception {
        List<CourseTeacher> courseTeachers = new ArrayList<>();
        int skipCount = 0;
            
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
                
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // 跳过标题行
                }
                    
                String[] fields = line.split(",", -1);
                if (fields.length >= 3) {
                    Integer courseId = Integer.parseInt(fields[0].trim());
                    Integer teacherId = Integer.parseInt(fields[1].trim());
                    String semester = fields[2].trim();
                    
                    // 检查是否已存在相同的关联（防止重复）
                    CourseTeacherId id = new CourseTeacherId(courseId, teacherId, semester);
                    if (courseTeacherRepository.existsById(id)) {
                        skipCount++;
                        continue; // 跳过重复的关联
                    }
                    
                    // 验证课程是否存在
                    if (!courseRepository.existsById(courseId)) {
                        System.err.println("课程不存在: " + courseId);
                        skipCount++;
                        continue;
                    }
                    
                    // 验证教师是否存在
                    if (!teacherRepository.existsById(teacherId)) {
                        System.err.println("教师不存在: " + teacherId);
                        skipCount++;
                        continue;
                    }
                    
                    CourseTeacher courseTeacher = new CourseTeacher();
                    courseTeacher.setCourseId(courseId);
                    courseTeacher.setTeacherId(teacherId);
                    courseTeacher.setSemester(semester);
                        
                    if (fields.length > 3 && !fields[3].trim().isEmpty()) {
                        courseTeacher.setRole(fields[3].trim());
                    }
                        
                    if (fields.length > 4) {
                        courseTeacher.setRemarks(fields[4].trim());
                    }
                        
                    courseTeachers.add(courseTeacher);
                }
            }
        }
            
        courseTeacherRepository.saveAll(courseTeachers);
        int importedCount = courseTeachers.size();
        System.out.println("课程-教师关联导入完成: 成功导入 " + importedCount + " 条，跳过重复 " + skipCount + " 条");
        return importedCount;
    }
}
