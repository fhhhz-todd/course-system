package com.example.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.course.entity.Submission;
import com.example.course.mapper.SubmissionMapper;
import com.example.course.service.SubmissionService;
import com.example.course.vo.SubmissionVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {
    
    @Override
    public boolean submitAssignment(Submission submission) {
        // 实现提交作业逻辑
        submission.setStatus(0); // 已提交状态
        return this.save(submission);
    }
    
    @Override
    public boolean updateSubmission(Submission submission) {
        // 实现更新提交逻辑
        return this.updateById(submission);
    }
    
    @Override
    public boolean gradeSubmission(Submission submission) {
        // 实现批阅逻辑
        submission.setStatus(1); // 已批阅状态
        return this.updateById(submission);
    }
    
    @Override
    public List<SubmissionVO> getSubmissionsByStudentId(String studentId) {
        // 实现获取学生提交记录逻辑
        return this.baseMapper.getSubmissionsByStudentId(studentId);
    }
    
    @Override
    public List<SubmissionVO> getSubmissionsByAssignmentId(Long assignmentId) {
        // 实现获取作业提交记录逻辑
        return this.baseMapper.getSubmissionsByAssignmentId(assignmentId);
    }
    
    @Override
    public SubmissionVO getSubmissionById(Long submissionId) {
        // 实现获取单个提交详情逻辑
        return this.baseMapper.getSubmissionById(submissionId);
    }
}