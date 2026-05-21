package com.fudan.campusinfo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseTeacherId implements Serializable {
    private Integer courseId;
    private Integer teacherId;
    private String semester;
}
