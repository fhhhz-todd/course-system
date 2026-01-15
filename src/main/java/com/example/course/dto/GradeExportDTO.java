package com.example.course.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 成绩导出Excel DTO
 * 第一列：学生ID
 * 第二列：真实姓名
 * 第三列：成绩
 */
@Data
public class GradeExportDTO {
    
    @ExcelProperty(index = 0, value = "学生ID")
    private String studentId;
    
    @ExcelProperty(index = 1, value = "真实姓名")
    private String studentName;
    
    @ExcelProperty(index = 2, value = "成绩")
    private Double score;
}




