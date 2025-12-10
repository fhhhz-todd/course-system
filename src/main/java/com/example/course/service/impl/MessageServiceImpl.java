package com.example.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.course.entity.Message;
import com.example.course.mapper.MessageMapper;
import com.example.course.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public List<Message> getMessagesByUserId(String userId) {
        return messageMapper.selectByUserId(userId);
    }

    @Override
    public Message createMessage(String userId, String title, String content, String sender) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setSender(sender);
        message.setStatus(0); // 默认未读
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        
        this.save(message);
        return message;
    }

    @Override
    public boolean markAsRead(Long messageId) {
        Message message = this.getById(messageId);
        if (message != null) {
            message.setStatus(1); // 标记为已读
            message.setUpdateTime(LocalDateTime.now());
            return this.updateById(message);
        }
        return false;
    }
    
    @Override
    public boolean deleteMessagesByIds(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return false;
        }
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", messageIds);
        return this.remove(queryWrapper);
    }
    
    @Override
    public boolean sendMessagesToStudents(List<String> studentIds, String title, String content, String sender) {
        if (studentIds == null || studentIds.isEmpty()) {
            return false;
        }
        
        boolean allSuccess = true;
        for (String studentId : studentIds) {
            Message message = new Message();
            message.setUserId(studentId);
            message.setTitle(title);
            message.setContent(content);
            message.setSender(sender);
            message.setStatus(0); // 默认未读
            message.setCreateTime(LocalDateTime.now());
            message.setUpdateTime(LocalDateTime.now());
            
            if (!this.save(message)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    @Override
    public boolean sendMessagesToAll(String title, String content, String sender) {
        // 获取所有用户的ID（学生、教师、管理员）
        List<String> allUserIds = messageMapper.selectAllUserIds();
        
        boolean allSuccess = true;
        for (String userId : allUserIds) {
            Message message = new Message();
            message.setUserId(userId);
            message.setTitle(title);
            message.setContent(content);
            message.setSender(sender);
            message.setStatus(0); // 默认未读
            message.setCreateTime(LocalDateTime.now());
            message.setUpdateTime(LocalDateTime.now());
            
            if (!this.save(message)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    @Override
    public boolean sendMessagesToTeachers(String title, String content, String sender) {
        // 获取所有教师的ID
        List<String> teacherUserIds = messageMapper.selectUserIdsByRole(2); // 角色2为教师
        
        boolean allSuccess = true;
        for (String userId : teacherUserIds) {
            Message message = new Message();
            message.setUserId(userId);
            message.setTitle(title);
            message.setContent(content);
            message.setSender(sender);
            message.setStatus(0); // 默认未读
            message.setCreateTime(LocalDateTime.now());
            message.setUpdateTime(LocalDateTime.now());
            
            if (!this.save(message)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    @Override
    public boolean sendMessagesToAllStudents(String title, String content, String sender) {
        // 获取所有学生的ID
        List<String> studentUserIds = messageMapper.selectUserIdsByRole(3); // 角色3为学生
        
        boolean allSuccess = true;
        for (String userId : studentUserIds) {
            Message message = new Message();
            message.setUserId(userId);
            message.setTitle(title);
            message.setContent(content);
            message.setSender(sender);
            message.setStatus(0); // 默认未读
            message.setCreateTime(LocalDateTime.now());
            message.setUpdateTime(LocalDateTime.now());
            
            if (!this.save(message)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    @Override
    public boolean sendMessagesToAdmins(String title, String content, String sender) {
        // 获取所有管理员的ID（角色1为管理员）
        List<String> adminUserIds = messageMapper.selectUserIdsByRole(1);
        
        boolean allSuccess = true;
        for (String userId : adminUserIds) {
            Message message = new Message();
            message.setUserId(userId);
            message.setTitle(title);
            message.setContent(content);
            message.setSender(sender);
            message.setStatus(0); // 默认未读
            message.setCreateTime(LocalDateTime.now());
            message.setUpdateTime(LocalDateTime.now());
            
            if (!this.save(message)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    @Override
    public boolean sendMessagesToUsers(List<String> userIds, String title, String content, String sender) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }
        
        boolean allSuccess = true;
        for (String userId : userIds) {
            Message message = new Message();
            message.setUserId(userId);
            message.setTitle(title);
            message.setContent(content);
            message.setSender(sender);
            message.setStatus(0); // 默认未读
            message.setCreateTime(LocalDateTime.now());
            message.setUpdateTime(LocalDateTime.now());
            
            if (!this.save(message)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    @Override
    public List<Message> searchMessages(String userId, String keyword, int page, int size) {
        return messageMapper.searchMessages(userId, keyword, page * size, size);
    }
}