package com.example.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.course.entity.Submission;
import com.example.course.vo.SubmissionVO;

import java.util.List;

public interface SubmissionService extends IService<Submission> {
    
    /**
     * 提交作业
     */
    boolean submitAssignment(Submission submission);
    
    /**
     * 撤回/修改作业
     */
    boolean updateSubmission(Submission submission);
    
    /**
     * 批阅作业
     */
    boolean gradeSubmission(Submission submission);
    
    /**
     * 获取学生作业提交记录
     */
    List<SubmissionVO> getSubmissionsByStudentId(String studentId);
    
    /**
     * 获取作业的所有提交记录
     */
    List<SubmissionVO> getSubmissionsByAssignmentId(Long assignmentId);
    
    /**
     * 根据ID获取提交详情
     */
    SubmissionVO getSubmissionById(Long submissionId);
}