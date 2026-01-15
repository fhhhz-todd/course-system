package com.example.course.dto;

import lombok.Data;

/**
 * 通过assignmentId和studentId打分的请求DTO
 */
@Data
public class GradeByAssignmentStudentRequest {
    private Long assignmentId;
    private String studentId;
    private Double score;
    private String comment;
}







