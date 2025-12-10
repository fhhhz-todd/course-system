package com.example.course.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class StudentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    // 注意：用户ID在数据库中是字符串（例如 stu00001），不能用 Long，否则会出现类型转换错误
    private String id;
    private String username;
    private String realName;
    private String studentUsername;
    private String studentName;
    private Integer role;
}