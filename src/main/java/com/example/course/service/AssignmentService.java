package com.example.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.course.entity.Assignment;
import com.example.course.vo.AssignmentVO;

import java.util.List;

public interface AssignmentService extends IService<Assignment> {
    
    /**
     * 发布作业
     */
    boolean publishAssignment(Assignment assignment);
    
    /**
     * 获取课程的所有作业
     */
    List<AssignmentVO> getAssignmentsByCourseId(Long courseId);
    
    /**
     * 删除作业
     */
    boolean deleteAssignment(Long assignmentId);
    
    /**
     * 获取作业详情
     */
    AssignmentVO getAssignmentDetail(Long assignmentId);
}