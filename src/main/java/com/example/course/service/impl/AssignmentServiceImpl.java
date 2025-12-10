package com.example.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.course.entity.Assignment;
import com.example.course.entity.Course;
import com.example.course.mapper.AssignmentMapper;
import com.example.course.service.AssignmentService;
import com.example.course.service.CourseService;
import com.example.course.vo.AssignmentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements AssignmentService {
    
    @Autowired
    private CourseService courseService;
    
    @Override
    public boolean publishAssignment(Assignment assignment) {
        // 实现发布作业逻辑
        // 验证课程ID是否有效
        if (assignment.getCourseId() == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        
        // 验证课程是否存在
        Course course = courseService.getById(assignment.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("指定的课程不存在: ID=" + assignment.getCourseId());
        }
        
        return this.save(assignment);
    }
    
    @Override
    public List<AssignmentVO> getAssignmentsByCourseId(Long courseId) {
        // 实现获取课程作业逻辑
        return this.baseMapper.getAssignmentsByCourseId(courseId);
    }
    
    @Override
    public boolean deleteAssignment(Long assignmentId) {
        // 实现删除作业逻辑
        return this.removeById(assignmentId);
    }
    
    @Override
    public AssignmentVO getAssignmentDetail(Long assignmentId) {
        // 实现获取作业详情逻辑
        return this.baseMapper.getAssignmentDetail(assignmentId);
    }
}