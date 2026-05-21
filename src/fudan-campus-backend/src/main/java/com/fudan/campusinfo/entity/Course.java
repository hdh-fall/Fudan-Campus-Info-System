package com.fudan.campusinfo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Integer courseId;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(precision = 3, scale = 1)
    private BigDecimal credits;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "semester_offered", length = 20)
    private String semesterOffered;
}
