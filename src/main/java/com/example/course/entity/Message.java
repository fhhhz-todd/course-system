package com.example.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("messages")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String userId; // 接收消息的用户ID（格式化ID）
    
    private String title; // 消息标题
    
    private String content; // 消息内容
    
    private String sender; // 发件人
    
    private Integer status; // 消息状态: 0-未读, 1-已读
    
    private LocalDateTime createTime; // 创建时间
    
    private LocalDateTime updateTime; // 更新时间
}