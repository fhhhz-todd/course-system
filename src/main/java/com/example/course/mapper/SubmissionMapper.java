package com.example.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.course.entity.Submission;
import com.example.course.vo.SubmissionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {
    List<SubmissionVO> getSubmissionsByStudentId(String studentId);
    List<SubmissionVO> getSubmissionsByAssignmentId(Long assignmentId);
    
    SubmissionVO getSubmissionById(Long submissionId);
}