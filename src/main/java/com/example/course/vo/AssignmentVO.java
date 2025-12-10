package com.example.course.vo;

import com.example.course.entity.Submission;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssignmentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String courseName;
    private LocalDateTime deadline;
    private String attachmentPath;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Submission> submissions;
    private Integer submissionCount;
    private Integer totalStudents;
}