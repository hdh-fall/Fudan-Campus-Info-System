package com.fudan.campusinfo.service;

import com.fudan.campusinfo.entity.*;
import com.fudan.campusinfo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class CampusInfoService {

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
    private CourseTeacherRepository courseTeacherRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QueryRecordRepository queryRecordRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    // ==================== 校区相关 ====================
    public List<Campus> getAllCampuses() {
        return campusRepository.findAll();
    }

    public Optional<Campus> getCampusById(Integer id) {
        return campusRepository.findById(id);
    }

    // ==================== 建筑相关 ====================
    public List<Building> getBuildingsByCampusId(Integer campusId) {
        return buildingRepository.findByCampus_CampusId(campusId);
    }

    public List<Building> searchBuildings(String keyword) {
        return buildingRepository.findByNameContaining(keyword);
    }

    public Optional<Building> getBuildingById(Integer id) {
        return buildingRepository.findById(id);
    }

    public Building createBuilding(Building building) {
        return buildingRepository.save(building);
    }

    public Building updateBuilding(Integer id, Building building) {
        if (!buildingRepository.existsById(id)) {
            throw new RuntimeException("建筑不存在: " + id);
        }
        building.setBuildingId(id);
        return buildingRepository.save(building);
    }

    public void deleteBuilding(Integer id) {
        buildingRepository.deleteById(id);
    }

    // ==================== 设施相关 ====================
    public List<Facility> getFacilitiesByBuildingId(Integer buildingId) {
        return facilityRepository.findByBuilding_BuildingId(buildingId);
    }

    public List<Facility> getFacilitiesByType(String type) {
        return facilityRepository.findByType(type);
    }

    public List<Facility> searchFacilities(String keyword) {
        return facilityRepository.findByNameContaining(keyword);
    }

    public Optional<Facility> getFacilityById(Integer id) {
        return facilityRepository.findById(id);
    }

    public Facility createFacility(Facility facility) {
        return facilityRepository.save(facility);
    }

    public Facility updateFacility(Integer id, Facility facility) {
        if (!facilityRepository.existsById(id)) {
            throw new RuntimeException("设施不存在: " + id);
        }
        facility.setFacilityId(id);
        return facilityRepository.save(facility);
    }

    public void deleteFacility(Integer id) {
        facilityRepository.deleteById(id);
    }

    // ==================== 课程相关 ====================
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getCoursesByDepartmentId(Integer departmentId) {
        return courseRepository.findByDepartment_DepartmentId(departmentId);
    }

    public List<Course> searchCourses(String keyword) {
        return courseRepository.findByNameContaining(keyword);
    }

    public Optional<Course> getCourseById(Integer id) {
        return courseRepository.findById(id);
    }

    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    public Course updateCourse(Integer id, Course course) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("课程不存在: " + id);
        }
        course.setCourseId(id);
        return courseRepository.save(course);
    }

    public void deleteCourse(Integer id) {
        courseRepository.deleteById(id);
    }

    /**
     * 获取所有课程及其授课教师信息
     */
    public List<Map<String, Object>> getAllCoursesWithTeachers() {
        List<Course> courses = courseRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Course course : courses) {
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("courseId", course.getCourseId());
            courseMap.put("name", course.getName());
            courseMap.put("department", course.getDepartment());
            courseMap.put("credits", course.getCredits());
            courseMap.put("semesterOffered", course.getSemesterOffered());
            courseMap.put("description", course.getDescription());
            
            // 获取该课程的所有授课教师
            List<CourseTeacher> courseTeachers = courseTeacherRepository.findByCourseId(course.getCourseId());
            
            // 使用Map去重，key为教师ID
            Map<Integer, Map<String, Object>> teacherMap = new HashMap<>();
            
            for (CourseTeacher ct : courseTeachers) {
                Integer teacherId = ct.getTeacher().getTeacherId();
                
                // 如果该教师已存在，合并学期信息
                if (teacherMap.containsKey(teacherId)) {
                    Map<String, Object> existingTeacher = teacherMap.get(teacherId);
                    // 将当前学期添加到已有教师的学期列表中
                    @SuppressWarnings("unchecked")
                    List<String> existingSemesters = (List<String>) existingTeacher.get("semesters");
                    if (!existingSemesters.contains(ct.getSemester())) {
                        existingSemesters.add(ct.getSemester());
                    }
                } else {
                    // 新教师，创建教师信息
                    Map<String, Object> teacherInfo = new HashMap<>();
                    teacherInfo.put("teacherId", teacherId);
                    teacherInfo.put("name", ct.getTeacher().getName());
                    teacherInfo.put("title", ct.getTeacher().getTitle());
                    teacherInfo.put("department", ct.getTeacher().getDepartment());
                    List<String> semesters = new ArrayList<>();
                    semesters.add(ct.getSemester());
                    teacherInfo.put("semesters", semesters);
                    teacherInfo.put("role", ct.getRole());
                    teacherMap.put(teacherId, teacherInfo);
                }
            }
            
            // 转换为列表
            List<Map<String, Object>> teachersList = new ArrayList<>(teacherMap.values());
            courseMap.put("teachers", teachersList);
            result.add(courseMap);
        }
        
        return result;
    }

    // ==================== 教师相关 ====================
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public List<Teacher> getTeachersByDepartmentId(Integer departmentId) {
        return teacherRepository.findByDepartment_DepartmentId(departmentId);
    }

    public List<Teacher> searchTeachers(String keyword) {
        return teacherRepository.findByNameContaining(keyword);
    }

    public Optional<Teacher> getTeacherById(Integer id) {
        return teacherRepository.findById(id);
    }

    public List<Course> getCoursesByTeacherId(Integer teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    public Teacher createTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public Teacher updateTeacher(Integer id, Teacher teacher) {
        if (!teacherRepository.existsById(id)) {
            throw new RuntimeException("教师不存在: " + id);
        }
        teacher.setTeacherId(id);
        return teacherRepository.save(teacher);
    }

    public void deleteTeacher(Integer id) {
        teacherRepository.deleteById(id);
    }

    // ==================== 活动相关 ====================
    public List<Event> getUpcomingEvents(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusDays(days);
        return eventRepository.findByEventTimeBetween(now, end);
    }

    public List<Event> getEventsByCampusId(Integer campusId) {
        return eventRepository.findByCampus_CampusId(campusId);
    }

    public List<Event> searchEvents(String keyword) {
        return eventRepository.findByNameContainingOrOrganizerContaining(keyword, keyword);
    }

    public Optional<Event> getEventById(Integer id) {
        return eventRepository.findById(id);
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public Event updateEvent(Integer id, Event event) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("活动不存在: " + id);
        }
        event.setEventId(id);
        return eventRepository.save(event);
    }

    public void deleteEvent(Integer id) {
        eventRepository.deleteById(id);
    }

    // ==================== 用户相关 ====================
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    // ==================== 查询记录相关 ====================
    public QueryRecord saveQueryRecord(QueryRecord record) {
        if (record.getQueryTime() == null) {
            record.setQueryTime(LocalDateTime.now());
        }
        return queryRecordRepository.save(record);
    }

    public List<QueryRecord> getQueryHistoryByUserId(Integer userId) {
        return queryRecordRepository.findByUser_UserIdOrderByQueryTimeDesc(userId);
    }

    public List<Object[]> getPopularCategories() {
        return queryRecordRepository.countByCategory();
    }

    // ==================== 院系相关 ====================
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
}
