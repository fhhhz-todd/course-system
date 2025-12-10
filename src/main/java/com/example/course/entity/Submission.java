package com.example.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("submission")
public class Submission implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作业ID
     */
    private Long assignmentId;

    /**
     * 学生ID（格式化ID）
     */
    private String studentId;

    /**
     * 提交内容
     */
    private String content;

    /**
     * 附件路径
     */
    private String attachmentPath;

    /**
     * 分数
     */
    private Double score;

    /**
     * 评语
     */
    private String comment;

    /**
     * 学生回复
     */
    private String reply;

    /**
     * 状态: 0-已提交, 1-已批阅
     */
    private Integer status;

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