package com.example.course.controller;

import com.example.course.common.Result;
import com.example.course.dto.MessageSendRequest;
import com.example.course.entity.Message;
import com.example.course.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/user/{userId}")
    public Result getMessagesByUser(@PathVariable String userId) {
        try {
            List<Message> messages = messageService.getMessagesByUserId(userId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取消息失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result getMessageById(@PathVariable Long id) {
        try {
            Message message = messageService.getById(id);
            if (message == null) {
                return Result.error("消息不存在");
            }
            return Result.success(message);
        } catch (Exception e) {
            return Result.error("获取消息失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/read")
    public Result markAsRead(@PathVariable Long id) {
        try {
            boolean result = messageService.markAsRead(id);
            if (result) {
                return Result.success("标记已读成功");
            } else {
                return Result.error("标记已读失败");
            }
        } catch (Exception e) {
            return Result.error("标记已读失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result createMessage(@RequestBody Message message) {
        try {
            messageService.save(message);
            return Result.success("消息创建成功");
        } catch (Exception e) {
            return Result.error("消息创建失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/batch")
    public Result deleteMessages(@RequestBody List<Long> messageIds) {
        try {
            boolean result = messageService.deleteMessagesByIds(messageIds);
            if (result) {
                return Result.success("消息批量删除成功");
            } else {
                return Result.error("消息批量删除失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除消息失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/send-to-students")
    public Result sendMessagesToStudents(@RequestBody MessageSendRequest request) {
        try {
            boolean result = messageService.sendMessagesToStudents(
                request.getReceiverIds(), 
                request.getTitle(), 
                request.getContent(), 
                request.getSender()
            );
            if (result) {
                return Result.success("消息发送成功");
            } else {
                return Result.error("消息发送失败");
            }
        } catch (Exception e) {
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/send")
    public Result sendMessage(@RequestBody MessageSendRequest request) {
        try {
            // 检查接收者类型并相应处理
            if (request.getType() == null) {
                // 默认行为：发送给指定用户
                if (request.getUserId() != null) {
                    Message message = new Message();
                    message.setUserId(request.getUserId());
                    message.setTitle(request.getTitle());
                    message.setContent(request.getContent());
                    message.setSender(request.getSender());
                    message.setStatus(0);
                    messageService.save(message);
                    return Result.success("消息发送成功");
                } else {
                    return Result.error("缺少接收者ID");
                }
            } else {
                // 根据类型发送消息
                boolean result = false;
                switch (request.getType()) {
                    case "all":
                        result = messageService.sendMessagesToAll(
                            request.getTitle(), 
                            request.getContent(), 
                            request.getSender()
                        );
                        break;
                    case "teachers":
                        result = messageService.sendMessagesToTeachers(
                            request.getTitle(), 
                            request.getContent(), 
                            request.getSender()
                        );
                        break;
                    case "students":
                        // 发送给所有学生
                        result = messageService.sendMessagesToAllStudents(
                            request.getTitle(), 
                            request.getContent(), 
                            request.getSender()
                        );
                        break;
                    case "admins":
                        // 发送给所有管理员
                        result = messageService.sendMessagesToAdmins(
                            request.getTitle(),
                            request.getContent(),
                            request.getSender()
                        );
                        break;
                    default:
                        return Result.error("未知的消息类型");
                }
                
                if (result) {
                    return Result.success("消息发送成功");
                } else {
                    return Result.error("消息发送失败");
                }
            }
        } catch (Exception e) {
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}/search")
    public Result searchMessages(@PathVariable String userId, 
                                @RequestParam(required = false) String keyword, 
                                @RequestParam(defaultValue = "0") int page, 
                                @RequestParam(defaultValue = "20") int size) {
        try {
            List<Message> messages = messageService.searchMessages(userId, keyword, page, size);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("搜索消息失败: " + e.getMessage());
        }
    }
}
