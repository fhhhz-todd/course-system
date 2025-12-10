package com.example.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.course.entity.Course;
import com.example.course.vo.CourseVO;

import java.util.List;

public interface CourseService extends IService<Course> {
    
    /**
     * 获取教师的所有课程
     */
    List<CourseVO> getCoursesByTeacherId(String teacherId);
    
    /**
     * 添加课程
     */
    boolean addCourse(Course course);
    
    /**
     * 删除课程
     */
    boolean deleteCourse(Long courseId);
    
    /**
     * 获取所有课程（供学生选择）
     */
    List<CourseVO> getAllCourses();
    
    /**
     * 根据ID获取单个课程详情
     */
    CourseVO getCourseById(Long courseId);
    
    /**
     * 获取课程总数
     */
    int countCourses();
}