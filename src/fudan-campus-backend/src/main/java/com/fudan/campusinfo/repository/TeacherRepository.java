package com.fudan.campusinfo.repository;

import com.fudan.campusinfo.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
    List<Teacher> findByDepartment_DepartmentId(Integer departmentId);
    List<Teacher> findByNameContaining(String keyword);
}
