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

    /**
     * 从CSV文件批量导入建筑数据
     * CSV格式: name,type,campus_id,floors,description
     */
    @Transactional
    public int importBuildings(MultipartFile file) throws Exception {
        List<Building> buildings = new ArrayList<>();
        
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
                    Building building = new Building();
                    building.setName(fields[0].trim());
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
                    
                    if (fields.length > 4) {
                        building.setDescription(fields[4].trim());
                    }
                    
                    buildings.add(building);
                }
            }
        }
        
        buildingRepository.saveAll(buildings);
        return buildings.size();
    }

    /**
     * 从CSV文件批量导入设施数据
     * CSV格式: name,type,building_id,location_desc,capacity,description
     */
    @Transactional
    public int importFacilities(MultipartFile file) throws Exception {
        List<Facility> facilities = new ArrayList<>();
        
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
                    Facility facility = new Facility();
                    facility.setName(fields[0].trim());
                    facility.setType(fields[1].trim());
                    
                    if (!fields[2].trim().isEmpty()) {
                        Integer buildingId = Integer.parseInt(fields[2].trim());
                        Building building = buildingRepository.findById(buildingId)
                            .orElseThrow(() -> new RuntimeException("建筑不存在: " + buildingId));
                        facility.setBuilding(building);
                    }
                    
                    if (fields.length > 3) {
                        facility.setLocationDesc(fields[3].trim());
                    }
                    
                    if (fields.length > 4 && !fields[4].trim().isEmpty()) {
                        facility.setCapacity(Integer.parseInt(fields[4].trim()));
                    }
                    
                    if (fields.length > 5) {
                        facility.setDescription(fields[5].trim());
                    }
                    
                    facilities.add(facility);
                }
            }
        }
        
        facilityRepository.saveAll(facilities);
        return facilities.size();
    }

    /**
     * 从CSV文件批量导入课程数据
     * CSV格式: name,department_id,credits,semester_offered,description
     */
    @Transactional
    public int importCourses(MultipartFile file) throws Exception {
        List<Course> courses = new ArrayList<>();
        
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
                    Course course = new Course();
                    course.setName(fields[0].trim());
                    
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
        return courses.size();
    }

    /**
     * 从CSV文件批量导入教师数据
     * CSV格式: name,department_id,title,email,phone
     */
    @Transactional
    public int importTeachers(MultipartFile file) throws Exception {
        List<Teacher> teachers = new ArrayList<>();
        
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
                    Teacher teacher = new Teacher();
                    teacher.setName(fields[0].trim());
                    
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
        return teachers.size();
    }

    /**
     * 从CSV文件批量导入活动数据
     * CSV格式: name,event_time,location_desc,campus_id,organizer,category,description
     */
    @Transactional
    public int importEvents(MultipartFile file) throws Exception {
        List<Event> events = new ArrayList<>();
        
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
                    Event event = new Event();
                    event.setName(fields[0].trim());
                    
                    if (!fields[1].trim().isEmpty()) {
                        event.setEventTime(java.time.LocalDateTime.parse(fields[1].trim()));
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
                    
                    if (fields.length > 4) {
                        event.setOrganizer(fields[4].trim());
                    }
                    
                    if (fields.length > 5) {
                        event.setCategory(fields[5].trim());
                    }
                    
                    if (fields.length > 6) {
                        event.setDescription(fields[6].trim());
                    }
                    
                    events.add(event);
                }
            }
        }
        
        eventRepository.saveAll(events);
        return events.size();
    }
}
