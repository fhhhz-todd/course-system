package com.example.course.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SubmissionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private Long studentId;
    private String studentName;
    private String content;
    private String attachmentPath;
    private Double score;
    private String comment;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}