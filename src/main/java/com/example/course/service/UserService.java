package com.example.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.course.dto.LoginRequest;
import com.example.course.dto.RegisterRequest;
import com.example.course.entity.User;
import com.example.course.vo.CourseVO;

import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 用户登录验证
     */
    User login(LoginRequest request);

    /**
     * 用户注册
     */
    boolean register(RegisterRequest request);

    /**
     * 初始化/重置学生密码 (教师端/管理员端功能)
     */
    boolean resetStudentPassword(String studentId);

    /**
     * 管理员获取所有教师账号
     */
    List<User> getAllTeachers();

    /**
     * 管理员添加教师账号
     */
    boolean addTeacher(User user);

    /**
     * 管理员删除教师账号
     */
    boolean deleteTeacher(String teacherId);

    /**
     * 获取教师所教的课程
     */
    List<CourseVO> getTeacherCourses(String teacherId);

    /**
     * 管理员获取所有学生账号
     */
    List<User> getAllStudents();

    /**
     * 管理员添加学生账号
     */
    boolean addStudent(User user);

    /**
     * 管理员添加管理员账号
     */
    boolean addAdmin(User user);

    /**
     * 管理员删除用户账号
     */
    boolean deleteUser(String userId);

    /**
     * 搜索用户（学生或教师）
     */
    List<User> searchUsers(String keyword, Integer role);
    
    User getAdmin();
    
    int getStudentCourseCount(String studentId);
    
    int getTeacherCourseCount(String teacherId);
    
    int countStudents();
    
    int countTeachers();
    
    int countAdmins();
    
    /**
     * 迁移现有用户的username格式（stu001→stu00001, tea001→tea00001）
     * 将三位数字格式转换为五位数字格式
     */
    int migrateUsernameFormat();
}