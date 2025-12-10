package com.example.course.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CourseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String courseName;
    private String description;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}