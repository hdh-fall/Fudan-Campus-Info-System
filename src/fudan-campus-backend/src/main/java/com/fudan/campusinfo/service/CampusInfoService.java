package com.fudan.campusinfo.service;

import com.fudan.campusinfo.entity.*;
import com.fudan.campusinfo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
        // 检查是否已存在同名建筑
        List<Building> existing = buildingRepository.findByNameContaining(building.getName());
        for (Building b : existing) {
            if (b.getName().equals(building.getName())) {
                throw new RuntimeException("建筑已存在: " + building.getName());
            }
        }
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
        // 检查是否已存在同名设施
        List<Facility> existing = facilityRepository.findByNameContaining(facility.getName());
        for (Facility f : existing) {
            if (f.getName().equals(facility.getName())) {
                throw new RuntimeException("设施已存在: " + facility.getName());
            }
        }
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
        // 检查是否已存在同名课程
        List<Course> existing = courseRepository.findByNameContaining(course.getName());
        for (Course c : existing) {
            if (c.getName().equals(course.getName())) {
                throw new RuntimeException("课程已存在: " + course.getName());
            }
        }
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
        // 获取所有课程-教师关联记录
        List<CourseTeacher> allCourseTeachers = courseTeacherRepository.findAll();
        
        // 使用Map进行分组：key为(courseId + semester)，value为课程信息
        Map<String, Map<String, Object>> courseSemesterMap = new LinkedHashMap<>();
        
        for (CourseTeacher ct : allCourseTeachers) {
            Course course = ct.getCourse();
            String key = course.getCourseId() + "_" + ct.getSemester();
            
            // 如果该课程在该学期不存在，创建新的课程记录
            if (!courseSemesterMap.containsKey(key)) {
                Map<String, Object> courseMap = new HashMap<>();
                courseMap.put("courseId", course.getCourseId());
                courseMap.put("name", course.getName());
                courseMap.put("department", course.getDepartment());
                courseMap.put("credits", course.getCredits());
                courseMap.put("semesterOffered", ct.getSemester()); // 使用实际的开课学期
                courseMap.put("description", course.getDescription());
                courseMap.put("teachers", new ArrayList<Map<String, Object>>());
                courseSemesterMap.put(key, courseMap);
            }
            
            // 添加教师信息到对应的课程学期记录中
            Map<String, Object> courseMap = courseSemesterMap.get(key);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> teachersList = (List<Map<String, Object>>) courseMap.get("teachers");
            
            // 检查该教师是否已在此课程学期记录中
            boolean teacherExists = false;
            for (Map<String, Object> teacher : teachersList) {
                if (teacher.get("teacherId").equals(ct.getTeacher().getTeacherId())) {
                    teacherExists = true;
                    break;
                }
            }
            
            if (!teacherExists) {
                Map<String, Object> teacherInfo = new HashMap<>();
                teacherInfo.put("teacherId", ct.getTeacher().getTeacherId());
                teacherInfo.put("name", ct.getTeacher().getName());
                teacherInfo.put("title", ct.getTeacher().getTitle());
                teacherInfo.put("department", ct.getTeacher().getDepartment());
                teacherInfo.put("semesters", Arrays.asList(ct.getSemester()));
                teacherInfo.put("role", ct.getRole());
                teachersList.add(teacherInfo);
            }
        }
        
        return new ArrayList<>(courseSemesterMap.values());
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
        // 检查是否已存在同名教师
        List<Teacher> existing = teacherRepository.findByNameContaining(teacher.getName());
        for (Teacher t : existing) {
            if (t.getName().equals(teacher.getName())) {
                throw new RuntimeException("教师已存在: " + teacher.getName());
            }
        }
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
    
    /**
     * 获取所有活动（包括已过期的）
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
    
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
        // 检查是否已存在同名活动
        List<Event> existing = eventRepository.findByNameContaining(event.getName());
        for (Event e : existing) {
            if (e.getName().equals(event.getName())) {
                throw new RuntimeException("活动已存在: " + event.getName());
            }
        }
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

    // ==================== 课程-教师关联相关 ====================
    
    /**
     * 获取所有课程-教师关联记录
     */
    public List<CourseTeacher> getAllCourseTeachers() {
        return courseTeacherRepository.findAll();
    }
    
    /**
     * 根据课程ID获取授课教师列表
     */
    public List<CourseTeacher> getTeachersByCourseId(Integer courseId) {
        return courseTeacherRepository.findByCourseId(courseId);
    }
    
    /**
     * 根据教师ID获取授课课程关联列表（包含学期、角色等完整信息）
     */
    public List<CourseTeacher> getCourseRelationsByTeacherId(Integer teacherId) {
        return courseTeacherRepository.findByTeacherId(teacherId);
    }
    
    /**
     * 创建课程-教师关联
     */
    public CourseTeacher createCourseTeacher(CourseTeacher courseTeacher) {
        // 检查是否已存在相同的关联（同一课程、同一教师、同一学期）
        Optional<CourseTeacher> existing = courseTeacherRepository.findById(
            new CourseTeacherId(
                courseTeacher.getCourseId(),
                courseTeacher.getTeacherId(),
                courseTeacher.getSemester()
            )
        );
        
        if (existing.isPresent()) {
            throw new RuntimeException("该教师在此学期已关联此课程");
        }
        
        // 验证课程和教师是否存在
        if (!courseRepository.existsById(courseTeacher.getCourseId())) {
            throw new RuntimeException("课程不存在: " + courseTeacher.getCourseId());
        }
        
        if (!teacherRepository.existsById(courseTeacher.getTeacherId())) {
            throw new RuntimeException("教师不存在: " + courseTeacher.getTeacherId());
        }
        
        return courseTeacherRepository.save(courseTeacher);
    }
    
    /**
     * 更新课程-教师关联（支持修改复合主键）
     */
    public CourseTeacher updateCourseTeacher(CourseTeacherId oldId, CourseTeacher courseTeacher) {
        // 检查旧记录是否存在
        if (!courseTeacherRepository.existsById(oldId)) {
            throw new RuntimeException("课程-教师关联不存在");
        }
        
        // 如果主键发生变化，需要检查新主键是否已存在
        CourseTeacherId newId = new CourseTeacherId(
            courseTeacher.getCourseId(),
            courseTeacher.getTeacherId(),
            courseTeacher.getSemester()
        );
        
        // 如果新旧主键不同，检查新主键是否已被占用
        if (!oldId.equals(newId)) {
            Optional<CourseTeacher> existing = courseTeacherRepository.findById(newId);
            if (existing.isPresent()) {
                throw new RuntimeException("该教师在此学期已关联此课程");
            }
            
            // 删除旧记录
            courseTeacherRepository.deleteById(oldId);
        }
        
        // 验证课程和教师是否存在
        if (!courseRepository.existsById(courseTeacher.getCourseId())) {
            throw new RuntimeException("课程不存在: " + courseTeacher.getCourseId());
        }
        
        if (!teacherRepository.existsById(courseTeacher.getTeacherId())) {
            throw new RuntimeException("教师不存在: " + courseTeacher.getTeacherId());
        }
        
        return courseTeacherRepository.save(courseTeacher);
    }
    
    /**
     * 删除课程-教师关联
     */
    public void deleteCourseTeacher(CourseTeacherId id) {
        if (!courseTeacherRepository.existsById(id)) {
            throw new RuntimeException("课程-教师关联不存在");
        }
        courseTeacherRepository.deleteById(id);
    }
}
