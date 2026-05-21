package com.fudan.campusinfo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "course_teacher")
@IdClass(CourseTeacherId.class)
public class CourseTeacher {
    @Id
    @Column(name = "course_id")
    private Integer courseId;

    @Id
    @Column(name = "teacher_id")
    private Integer teacherId;

    @Id
    @Column(name = "semester", length = 20)
    private String semester;

    @Column(length = 30)
    private String role = "主讲";

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    private Teacher teacher;
}
