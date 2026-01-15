package com.example.course.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 学生导入Excel DTO
 */
@Data
public class StudentImportDTO {
    
    @ExcelProperty(index = 0, value = "学生ID")
    private String studentId;
}


