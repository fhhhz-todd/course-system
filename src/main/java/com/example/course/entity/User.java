package com.example.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("\"USER\"")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（格式化ID：stu00001/tea00001格式）
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码(MD5加密)
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色: 1-管理员, 2-教师, 3-学生
     */
    private Integer role;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}