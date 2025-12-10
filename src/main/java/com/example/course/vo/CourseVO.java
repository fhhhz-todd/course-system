package com.example.course.vo;

import com.example.course.entity.Assignment;
import com.example.course.entity.User;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class CourseVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private String description;
    private String teacherId;
    private String teacherName;
    private List<Assignment> assignments;
    private Integer studentCount;
    private List<User> students; // 存储选课学生列表
}