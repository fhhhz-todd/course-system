package com.example.course.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AssignmentDTO {
    private Long id;
    private String title;
    private String description;
    private Long courseId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline; // 使用LocalDate来接收前端的日期
    
    // 转换为Assignment实体类的方法
    public com.example.course.entity.Assignment toEntity() {
        com.example.course.entity.Assignment assignment = new com.example.course.entity.Assignment();
        assignment.setId(this.id);
        assignment.setTitle(this.title);
        assignment.setDescription(this.description);
        assignment.setCourseId(this.courseId);
        
        // 将LocalDate转换为LocalDateTime，时间为当天的00:00:00
        if (this.deadline != null) {
            assignment.setDeadline(this.deadline.atStartOfDay());
        }
        
        return assignment;
    }
}