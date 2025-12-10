package com.example.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.course.entity.Assignment;
import com.example.course.vo.AssignmentVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AssignmentMapper extends BaseMapper<Assignment> {
    List<AssignmentVO> getAssignmentsByCourseId(Long courseId);
    AssignmentVO getAssignmentDetail(Long assignmentId);
}