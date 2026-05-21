package com.fudan.campusinfo.repository;

import com.fudan.campusinfo.entity.CourseTeacher;
import com.fudan.campusinfo.entity.CourseTeacherId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseTeacherRepository extends JpaRepository<CourseTeacher, CourseTeacherId> {
    List<CourseTeacher> findByTeacherId(Integer teacherId);
    List<CourseTeacher> findByCourseId(Integer courseId);
}
