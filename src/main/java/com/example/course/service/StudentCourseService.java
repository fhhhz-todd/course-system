package com.example.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.course.entity.StudentCourse;
import com.example.course.vo.CourseVO;
import com.example.course.vo.StudentVO;

import java.util.List;

public interface StudentCourseService extends IService<StudentCourse> {
    
    /**
     * 根据课程ID获取选课学生列表
     */
    List<StudentVO> getStudentsByCourseId(Long courseId);
    
    /**
     * 根据学生ID和课程ID删除选课记录（踢出学生）
     */
    boolean deleteStudentFromCourse(String studentId, Long courseId);
    
    /**
     * 添加学生到课程
     */
    boolean addStudentToCourse(String studentId, Long courseId);
    
    /**
     * 批量添加学生到课程
     */
    boolean addStudentsToCourse(List<String> studentIds, Long courseId);
    
    /**
     * 批量从课程中删除学生
     */
    boolean removeStudentsFromCourse(List<String> studentIds, Long courseId);
    
    /**
     * 从课程中删除单个学生
     */
    boolean removeStudentFromCourse(String studentId, Long courseId);
    
    /**
     * 根据学生ID获取已选课程列表
     */
    List<CourseVO> getCoursesByStudentId(String studentId);
}