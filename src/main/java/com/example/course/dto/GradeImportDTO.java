package com.example.course.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 成绩导入Excel DTO
 * 第一列：用户标识（可以是学生ID、用户名或真实姓名）
 * 第二列：分数
 */
@Data
public class GradeImportDTO {
    
    @ExcelProperty(index = 0, value = "用户标识")
    private String userIdentifier;
    
    @ExcelProperty(index = 1, value = "分数")
    private Double score;
    
    @ExcelProperty(index = 2, value = "评语")
    private String comment;
}




