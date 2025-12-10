package com.example.course.controller;

import cn.hutool.crypto.SecureUtil;
import com.example.course.common.Result;
import com.example.course.dto.LoginRequest;
import com.example.course.dto.RegisterRequest;
import com.example.course.entity.User;
import com.example.course.service.UserService;
import com.example.course.vo.CourseVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     *
     * @param request 注册请求参数
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request) {
        try {
            boolean result = userService.register(request);
            if (result) {
                return Result.success("注册成功");
            } else {
                return Result.error("注册失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录接口
     *
     * @param request 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<User> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.login(request);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户信息（用于前端刷新最新资料）
     */
    @GetMapping("/{userId}")
    public Result<User> getUserById(@PathVariable String userId) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户基础信息（username/realName）
     * 注意：id 为格式化主键，不允许修改。
     */
    @PutMapping("/{userId}")
    public Result<User> updateUserProfile(@PathVariable String userId, @RequestBody User request) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 仅允许更新 username / realName（避免前端误传 password/role/id）
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (request.getRealName() == null || request.getRealName().trim().isEmpty()) {
                return Result.error("真实姓名不能为空");
            }

            String newUsername = request.getUsername().trim();
            String newRealName = request.getRealName().trim();

            // username 唯一性校验（排除自己）
            User existed = userService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                    .eq(User::getUsername, newUsername));
            if (existed != null && existed.getId() != null && !existed.getId().equals(userId)) {
                return Result.error("用户名已被占用");
            }

            user.setUsername(newUsername);
            user.setRealName(newRealName);

            boolean ok = userService.updateById(user);
            if (!ok) {
                return Result.error("更新失败");
            }

            // 返回最新用户信息（不包含密码）
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 重置学生密码接口
     *
     * @param studentId 学生ID
     * @return 重置结果
     */
    @PostMapping("/{studentId}/reset-password")
    public Result<String> resetStudentPassword(@PathVariable String studentId) {
        try {
            boolean result = userService.resetStudentPassword(studentId);
            if (result) {
                return Result.success("密码重置成功");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取教师所教的课程
     *
     * @param teacherId 教师ID
     * @return 课程列表
     */
    @GetMapping("/courses/{teacherId}")
    public Result<List<CourseVO>> getTeacherCourses(@PathVariable String teacherId) {
        try {
            List<CourseVO> courses = userService.getTeacherCourses(teacherId);
            return Result.success(courses);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有学生
     */
    @GetMapping("/students")
    public Result<List<User>> getAllStudents() {
        try {
            List<User> students = userService.getAllStudents();
            return Result.success(students);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有教师
     */
    @GetMapping("/teachers")
    public Result<List<User>> getAllTeachers() {
        try {
            List<User> teachers = userService.getAllTeachers();
            return Result.success(teachers);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 搜索用户
     */
    @GetMapping("/students/search")
    public Result<List<User>> searchStudents(@RequestParam String keyword) {
        try {
            List<User> users = userService.searchUsers(keyword, 3); // 3代表学生
            return Result.success(users);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 搜索教师
     */
    @GetMapping("/teachers/search")
    public Result<List<User>> searchTeachers(@RequestParam String keyword) {
        try {
            List<User> users = userService.searchUsers(keyword, 2); // 2代表教师
            return Result.success(users);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加用户
     */
    @PostMapping
    public Result<String> addUser(@RequestBody User user) {
        try {
            if (user.getRole() == 1) {
                // 管理员角色
                boolean result = userService.addAdmin(user);
                if (result) {
                    return Result.success("管理员添加成功");
                } else {
                    return Result.error("管理员添加失败");
                }
            } else if (user.getRole() == 2) {
                boolean result = userService.addTeacher(user);
                if (result) {
                    return Result.success("教师添加成功");
                } else {
                    return Result.error("教师添加失败");
                }
            } else if (user.getRole() == 3) {
                boolean result = userService.addStudent(user);
                if (result) {
                    return Result.success("学生添加成功");
                } else {
                    return Result.error("学生添加失败");
                }
            } else {
                return Result.error("角色类型错误");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public Result<String> deleteUser(@PathVariable String userId) {
        try {
            boolean result = userService.deleteUser(userId);
            if (result) {
                return Result.success("用户删除成功");
            } else {
                return Result.error("用户删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{userId}/reset-password")
    public Result<String> resetUserPassword(@PathVariable String userId, @RequestBody User request) {
        try {
            // 获取用户信息以检查角色
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 重置密码
            user.setPassword(SecureUtil.md5(request.getPassword()));
            boolean result = userService.updateById(user);
            if (result) {
                return Result.success("密码重置成功");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取管理员信息
     */
    @GetMapping("/admin")
    public Result<User> getAdmin() {
        try {
            User admin = userService.getAdmin();
            if (admin != null) {
                return Result.success(admin);
            } else {
                return Result.error("管理员不存在");
            }
        } catch (Exception e) {
            return Result.error("获取管理员信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学生的选课数量
     */
    @GetMapping("/{userId}/course-count")
    public Result<Integer> getUserCourseCount(@PathVariable String userId) {
        try {
            // 尝试获取教师课程数量
            int teacherCourseCount = userService.getTeacherCourseCount(userId);
            if (teacherCourseCount > 0) {
                return Result.success(teacherCourseCount);
            }
            
            // 如果不是教师，尝试获取学生选课数量
            int studentCourseCount = userService.getStudentCourseCount(userId);
            return Result.success(studentCourseCount);
        } catch (Exception e) {
            return Result.error("获取课程数量失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学生总数
     */
    @GetMapping("/students/count")
    public Result<Integer> getStudentCount() {
        try {
            int count = userService.countStudents();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取学生总数失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取教师总数
     */
    @GetMapping("/teachers/count")
    public Result<Integer> getTeacherCount() {
        try {
            int count = userService.countTeachers();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取教师总数失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取管理员总数
     */
    @GetMapping("/admins/count")
    public Result<Integer> getAdminCount() {
        try {
            int count = userService.countAdmins();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取管理员总数失败: " + e.getMessage());
        }
    }
}
