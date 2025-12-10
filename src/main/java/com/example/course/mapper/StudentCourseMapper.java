package com.example.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.course.entity.StudentCourse;
import com.example.course.vo.CourseVO;
import com.example.course.vo.StudentVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentCourseMapper extends BaseMapper<StudentCourse> {
    
    /**
     * 根据课程ID获取选课学生列表
     */
    @Select("SELECT u.id, u.username, u.real_name, u.role " +
            "FROM student_course sc " +
            "JOIN \"USER\" u ON sc.student_id = u.id " +
            "WHERE sc.course_id = #{courseId}")
    List<StudentVO> getStudentsByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 根据学生ID和课程ID删除选课记录
     */
    @Delete("DELETE FROM student_course WHERE student_id = #{studentId} AND course_id = #{courseId}")
    void deleteByStudentIdAndCourseId(@Param("studentId") String studentId, @Param("courseId") Long courseId);
    
    /**
     * 根据学生ID获取已选课程列表
     */
    @Select("SELECT c.id, c.course_code, c.course_name, c.credits, c.description, c.teacher_id, u.real_name AS teacher_name " +
            "FROM student_course sc " +
            "JOIN course c ON sc.course_id = c.id " +
            "LEFT JOIN \"USER\" u ON c.teacher_id = u.id " +
            "WHERE sc.student_id = #{studentId}")
    List<CourseVO> getCoursesByStudentId(@Param("studentId") String studentId);
    
    /**
     * 根据课程ID获取课程信息
     */
    @Select("SELECT c.id, c.course_code, c.course_name, c.credits, c.description, c.teacher_id, u.real_name AS teacher_name " +
            "FROM course c " +
            "LEFT JOIN \"USER\" u ON c.teacher_id = u.id " +
            "WHERE c.id = #{courseId}")
    CourseVO getCourseById(@Param("courseId") Long courseId);
    
    /**
     * 根据课程ID获取学生ID列表
     */
    List<String> selectStudentIdsByCourseId(@Param("courseId") Long courseId);
}