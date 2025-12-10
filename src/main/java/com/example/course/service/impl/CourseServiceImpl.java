package com.example.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.course.entity.Assignment;
import com.example.course.entity.Course;
import com.example.course.entity.StudentCourse;
import com.example.course.entity.Submission;
import com.example.course.mapper.AssignmentMapper;
import com.example.course.mapper.CourseMapper;
import com.example.course.mapper.StudentCourseMapper;
import com.example.course.mapper.SubmissionMapper;
import com.example.course.service.CourseService;
import com.example.course.vo.CourseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Autowired
    private AssignmentMapper assignmentMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private StudentCourseMapper studentCourseMapper;
    
    @Override
    public List<CourseVO> getCoursesByTeacherId(String teacherId) {
        // 实现获取教师课程逻辑
        return this.baseMapper.getCoursesByTeacherId(teacherId);
    }
    
    @Override
    public boolean addCourse(Course course) {
        // 检查是否已存在相同课程编号的课程
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_code", course.getCourseCode());
        if (this.count(queryWrapper) > 0) {
            throw new RuntimeException("课程编号已存在");
        }
        
        // 实现添加课程逻辑
        return this.save(course);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCourse(Long courseId) {
        // 1) 删除该课程下所有作业及其提交
        List<Assignment> assignments = assignmentMapper.selectList(
                new LambdaQueryWrapper<Assignment>().eq(Assignment::getCourseId, courseId));
        if (assignments != null && !assignments.isEmpty()) {
            List<Long> assignmentIds = assignments.stream()
                    .map(Assignment::getId)
                    .collect(Collectors.toList());

            // 删除 submission（按 assignment_id）
            submissionMapper.delete(new LambdaQueryWrapper<Submission>()
                    .in(Submission::getAssignmentId, assignmentIds));

            // 删除 assignment（按 course_id）
            assignmentMapper.delete(new LambdaQueryWrapper<Assignment>()
                    .eq(Assignment::getCourseId, courseId));
        }

        // 2) 删除 student_course 选课关系
        studentCourseMapper.delete(new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getCourseId, courseId));

        // 3) 删除 course 本身
        return this.removeById(courseId);
    }
    
    @Override
    public List<CourseVO> getAllCourses() {
        // 实现获取所有课程逻辑
        return this.baseMapper.getAllCourses();
    }
    
    @Override
    public CourseVO getCourseById(Long courseId) {
        // 实现获取单个课程详情逻辑
        return this.baseMapper.getCourseById(courseId);
    }
    
    @Override
    public int countCourses() {
        return Math.toIntExact(this.count());
    }
}