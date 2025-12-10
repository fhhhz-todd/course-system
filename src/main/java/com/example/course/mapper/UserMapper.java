package com.example.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.course.entity.User;
import com.example.course.vo.CourseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<CourseVO> getTeacherCourses(String teacherId);
    
    int getStudentCourseCount(String studentId);
    
    int getTeacherCourseCount(String teacherId);
    
    /**
     * 更新用户ID（主键）
     */
    @Update("UPDATE \"USER\" SET id = #{newId} WHERE id = #{oldId}")
    int updateUserId(@Param("oldId") String oldId, @Param("newId") String newId);
    
    /**
     * 更新course表中的teacher_id
     */
    @Update("UPDATE course SET teacher_id = #{newId} WHERE teacher_id = #{oldId}")
    int updateTeacherIdInCourse(@Param("oldId") String oldId, @Param("newId") String newId);
    
    /**
     * 更新student_course表中的student_id
     */
    @Update("UPDATE student_course SET student_id = #{newId} WHERE student_id = #{oldId}")
    int updateStudentIdInStudentCourse(@Param("oldId") String oldId, @Param("newId") String newId);
    
    /**
     * 更新submission表中的student_id
     */
    @Update("UPDATE submission SET student_id = #{newId} WHERE student_id = #{oldId}")
    int updateStudentIdInSubmission(@Param("oldId") String oldId, @Param("newId") String newId);
    
    /**
     * 更新messages表中的user_id
     */
    @Update("UPDATE messages SET user_id = #{newId} WHERE user_id = #{oldId}")
    int updateUserIdInMessages(@Param("oldId") String oldId, @Param("newId") String newId);
}