package com.example.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.course.entity.Course;
import com.example.course.vo.CourseVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    List<CourseVO> getAllCourses();
    List<CourseVO> getCoursesByTeacherId(String teacherId);
    CourseVO getCourseById(Long courseId);
}