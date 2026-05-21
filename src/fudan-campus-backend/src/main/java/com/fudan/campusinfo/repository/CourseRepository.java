package com.fudan.campusinfo.repository;

import com.fudan.campusinfo.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByDepartment_DepartmentId(Integer departmentId);
    List<Course> findByNameContaining(String keyword);
    
    @Query("SELECT c FROM Course c JOIN CourseTeacher ct ON c.courseId = ct.courseId WHERE ct.teacherId = :teacherId")
    List<Course> findByTeacherId(@Param("teacherId") Integer teacherId);
    
    @Query("SELECT c.department.name, COUNT(c) FROM Course c GROUP BY c.department.name")
    List<Object[]> countCoursesByDepartment();
}
