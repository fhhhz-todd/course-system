package com.example.course.service;

import com.example.course.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface MessageService extends IService<Message> {
    List<Message> getMessagesByUserId(String userId);
    
    Message createMessage(String userId, String title, String content, String sender);
    
    boolean markAsRead(Long messageId);
    
    boolean deleteMessagesByIds(List<Long> messageIds);
    
    boolean sendMessagesToStudents(List<String> studentIds, String title, String content, String sender);
    
    boolean sendMessagesToAll(String title, String content, String sender);
    
    boolean sendMessagesToTeachers(String title, String content, String sender);
    
    boolean sendMessagesToAllStudents(String title, String content, String sender);
    
    /**
     * 向所有管理员发送消息
     */
    boolean sendMessagesToAdmins(String title, String content, String sender);
    
    boolean sendMessagesToUsers(List<String> userIds, String title, String content, String sender);
    
    List<Message> searchMessages(String userId, String keyword, int page, int size);
}