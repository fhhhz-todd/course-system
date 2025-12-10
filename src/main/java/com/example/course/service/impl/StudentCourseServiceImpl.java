package com.example.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.course.entity.StudentCourse;
import com.example.course.mapper.StudentCourseMapper;
import com.example.course.service.MessageService;
import com.example.course.service.StudentCourseService;
import com.example.course.vo.CourseVO;
import com.example.course.vo.StudentVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentCourseServiceImpl extends ServiceImpl<StudentCourseMapper, StudentCourse> implements StudentCourseService {

    private static final Logger log = LoggerFactory.getLogger(StudentCourseServiceImpl.class);
    
    @Autowired
    private MessageService messageService;
    
    @Override
    public List<StudentVO> getStudentsByCourseId(Long courseId) {
        return this.baseMapper.getStudentsByCourseId(courseId);
    }
    
    @Override
    public boolean deleteStudentFromCourse(String studentId, Long courseId) {
        // 检查选课记录是否存在
        QueryWrapper<StudentCourse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId).eq("course_id", courseId);
        
        if (this.count(queryWrapper) > 0) {
            // 获取课程名称用于消息
            CourseVO course = this.baseMapper.getCourseById(courseId);
            String courseName = course != null ? course.getCourseName() : "未知课程";

            // 删除选课记录
            this.baseMapper.deleteByStudentIdAndCourseId(studentId, courseId);

            // 发送退课通知
            try {
                String title = "退课通知";
                String content = "你已退选" + courseName + "课程！";
                messageService.createMessage(studentId, title, content, "系统");
            } catch (Exception ex) {
                // 消息发送失败不应影响主流程（否则会出现“提示失败但实际已退课/已选课”的情况）
                log.warn("Failed to create course-withdraw message. studentId={}, courseId={}", studentId, courseId, ex);
            }

            return true;
        }

        // 幂等：记录不存在也视为“已退课”
        return true;
    }
    
    @Override
    public boolean addStudentToCourse(String studentId, Long courseId) {
        if (studentId == null || studentId.isBlank() || courseId == null) {
            log.warn("addStudentToCourse called with invalid params. studentId={}, courseId={}", studentId, courseId);
            return false;
        }
        // 检查是否已经选课
        QueryWrapper<StudentCourse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId).eq("course_id", courseId);
        
        if (this.count(queryWrapper) > 0) {
            // 幂等：已选直接返回成功，避免前端重复点击导致“提示失败但实际已选/已存在”
            return true;
        }
        
        // 获取课程名称用于消息
        CourseVO course = this.baseMapper.getCourseById(courseId);
        String courseName = course != null ? course.getCourseName() : "未知课程";
        
        // 添加选课记录
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudentId(studentId);
        studentCourse.setCourseId(courseId);
        boolean result;
        try {
            result = this.save(studentCourse);
        } catch (Exception ex) {
            // 避免“前端提示成功但实际没写入”的错觉：这里发生异常必须返回失败
            log.error("Failed to enroll course. studentId={}, courseId={}", studentId, courseId, ex);
            return false;
        }
        
        if (result) {
            // 保险：再查一次，确保数据库里确实存在这条关系（否则不返回成功）
            if (this.count(queryWrapper) == 0) {
                log.error("Enroll returned success but record not found after insert. studentId={}, courseId={}", studentId, courseId);
                return false;
            }
            // 发送选课成功通知
            try {
                String title = "选课成功通知";
                String content = "你已成功选择" + courseName + "课程！";
                messageService.createMessage(studentId, title, content, "系统");
            } catch (Exception ex) {
                // 消息发送失败不应影响选课结果
                log.warn("Failed to create course-enroll message. studentId={}, courseId={}", studentId, courseId, ex);
            }
        }
        
        return result;
    }
    
    @Override
    public boolean addStudentsToCourse(List<String> studentIds, Long courseId) {
        boolean allSuccess = true;
        for (String studentId : studentIds) {
            try {
                addStudentToCourse(studentId, courseId); // 直接调用带消息通知的方法
            } catch (RuntimeException e) {
                allSuccess = false; // 如果某个学生添加失败，继续处理其他学生
            }
        }
        return allSuccess;
    }
    
    @Override
    public boolean removeStudentsFromCourse(List<String> studentIds, Long courseId) {
        boolean allSuccess = true;
        for (String studentId : studentIds) {
            try {
                removeStudentFromCourse(studentId, courseId); // 直接调用带消息通知的方法
            } catch (RuntimeException e) {
                allSuccess = false; // 如果某个学生移除失败，继续处理其他学生
            }
        }
        return allSuccess;
    }
    
    @Override
    public boolean removeStudentFromCourse(String studentId, Long courseId) {
        // 检查选课记录是否存在
        QueryWrapper<StudentCourse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId).eq("course_id", courseId);
        
        if (this.count(queryWrapper) > 0) {
            // 获取课程名称用于消息
            CourseVO course = this.baseMapper.getCourseById(courseId);
            String courseName = course != null ? course.getCourseName() : "未知课程";

            // 删除选课记录
            this.remove(queryWrapper);

            // 发送退课通知
            try {
                String title = "退课通知";
                String content = "你已退选" + courseName + "课程！";
                messageService.createMessage(studentId, title, content, "系统");
            } catch (Exception ex) {
                // 消息发送失败不应影响主流程
                log.warn("Failed to create course-withdraw message. studentId={}, courseId={}", studentId, courseId, ex);
            }

            return true;
        }

        // 幂等：记录不存在也视为“已退课”
        return true;
    }
    
    @Override
    public List<CourseVO> getCoursesByStudentId(String studentId) {
        return this.baseMapper.getCoursesByStudentId(studentId);
    }
}