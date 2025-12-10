package com.example.course.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.course.dto.LoginRequest;
import com.example.course.dto.RegisterRequest;
import com.example.course.entity.Course;
import com.example.course.entity.User;
import com.example.course.entity.StudentCourse;
import com.example.course.entity.Submission;
import com.example.course.entity.Message;
import com.example.course.mapper.CourseMapper;
import com.example.course.mapper.MessageMapper;
import com.example.course.mapper.StudentCourseMapper;
import com.example.course.mapper.SubmissionMapper;
import com.example.course.mapper.UserMapper;
import com.example.course.service.CourseService;
import com.example.course.service.UserService;
import com.example.course.vo.CourseVO;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 默认重置密码 '123456' 的 MD5 值
    private static final String DEFAULT_PASSWORD_MD5 = SecureUtil.md5("123456");

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private StudentCourseMapper studentCourseMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public User login(LoginRequest request) {
        // 1. 根据用户名查找用户
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        // 2. 验证用户是否存在 (如果不存在，抛出异常)
        Assert.notNull(user, "用户名不存在");

        // 3. 验证密码 (将用户输入的密码进行 MD5 加密后与数据库存储的密码比对)
        String passwordMd5 = SecureUtil.md5(request.getPassword());
        Assert.isTrue(passwordMd5.equals(user.getPassword()), "密码错误");

        // 4. 返回用户数据（去除敏感信息）
        user.setPassword(null); // 安全考虑，不返回密码
        return user;
    }

    @Override
    public boolean register(RegisterRequest request) {
        // 1. 创建新用户，保持username不变
        User newUser = new User();
        newUser.setUsername(request.getUsername()); // 使用用户提供的username
        newUser.setRealName(request.getRealName());
        newUser.setPassword(SecureUtil.md5(request.getPassword())); // MD5加密存储
        newUser.setRole(request.getRole());
        
        // 2. 生成格式化ID（stu00001/tea00001格式）作为主键
        String formattedId = generateFormattedId(request.getRole());
        newUser.setId(formattedId);

        // 3. 保存用户
        return this.save(newUser);
    }
    
    /**
     * 生成格式化的ID（stu00001/tea00001格式：三个字母+五个数字）
     * @param role 角色：1-管理员，2-教师，3-学生
     * @return 格式化的ID
     */
    private String generateFormattedId(Integer role) {
        String prefix;
        if (role == 2) {
            prefix = "tea";
        } else if (role == 3) {
            prefix = "stu";
        } else if (role == 1) {
            prefix = "adm";
        } else {
            // 其他角色使用默认格式
            prefix = "user";
        }
        
        // 查询该角色下所有用户，找出最大编号（基于id主键）
        List<User> users = this.list(new LambdaQueryWrapper<User>()
                .eq(User::getRole, role)
                .isNotNull(User::getId)
                .likeRight(User::getId, prefix));
        
        int maxNumber = 0;
        for (User user : users) {
            String id = user.getId();
            if (id != null && id.startsWith(prefix)) {
                try {
                    String numberStr = id.substring(prefix.length());
                    int number = Integer.parseInt(numberStr);
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    // 忽略格式不正确的ID
                }
            }
        }
        
        // 生成下一个编号（5位数字，从00001开始）
        int nextNumber = maxNumber + 1;
        String generatedId = String.format("%s%05d", prefix, nextNumber);
        
        // 检查生成的ID是否已存在（防止并发情况下的冲突）
        User existingUser = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, generatedId));
        
        // 如果存在，继续递增直到找到一个不存在的ID
        while (existingUser != null) {
            nextNumber++;
            generatedId = String.format("%s%05d", prefix, nextNumber);
            existingUser = this.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getId, generatedId));
        }
        
        return generatedId;
    }

    @Override
    public boolean resetStudentPassword(String studentId) {
        User user = this.getById(studentId);
        Assert.notNull(user, "学生用户不存在");

        // 确保只有学生（role=3）才能重置密码
        Assert.isTrue(user.getRole().equals(3), "该用户不是学生角色");

        user.setPassword(DEFAULT_PASSWORD_MD5);
        return this.updateById(user);
    }

    @Override
    public List<User> getAllTeachers() {
        // 实现获取所有教师账号逻辑
        return this.list(new LambdaQueryWrapper<User>().eq(User::getRole, 2));
    }

    @Override
    public boolean addTeacher(User user) {
        // 实现添加教师账号逻辑
        user.setRole(2); // 教师权限
        user.setPassword(SecureUtil.md5("123456")); // 默认密码
        // 生成格式化ID作为主键
        String formattedId = generateFormattedId(2);
        user.setId(formattedId);
        // 如果username为空，使用formattedId作为username
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            user.setUsername(formattedId);
        }
        return this.save(user);
    }

    @Override
    public boolean deleteTeacher(String teacherId) {
        // 实现删除教师账号逻辑
        return this.removeById(teacherId);
    }

    @Override
    public List<CourseVO> getTeacherCourses(String teacherId) {
        // 实现获取教师所教课程逻辑
        return this.baseMapper.getTeacherCourses(teacherId);
    }

    @Override
    public List<User> getAllStudents() {
        // 实现获取所有学生账号逻辑
        return this.list(new LambdaQueryWrapper<User>().eq(User::getRole, 3));
    }

    @Override
    public boolean addStudent(User user) {
        // 实现添加学生账号逻辑
        user.setRole(3); // 学生权限
        user.setPassword(SecureUtil.md5("123456")); // 默认密码
        // 生成格式化ID作为主键
        String formattedId = generateFormattedId(3);
        user.setId(formattedId);
        // 如果username为空，使用formattedId作为username
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            user.setUsername(formattedId);
        }
        return this.save(user);
    }

    @Override
    public boolean addAdmin(User user) {
        // 实现添加管理员账号逻辑
        user.setRole(1); // 管理员权限
        user.setPassword(SecureUtil.md5("123456")); // 默认密码
        // 生成格式化ID作为主键
        String formattedId = generateFormattedId(1);
        user.setId(formattedId);
        // 如果username为空，使用formattedId作为username
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            user.setUsername(formattedId);
        }
        return this.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(String userId) {
        User user = this.getById(userId);
        Assert.notNull(user, "用户不存在");

        // 1) 先清理与用户相关的业务数据，避免外键/逻辑残留
        Integer role = user.getRole();
        if (role != null && role == 2) {
            // 教师：删除其所有课程（课程删除已级联清理作业/提交/选课关系）
            List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                    .eq(Course::getTeacherId, userId));
            if (courses != null) {
                for (Course c : courses) {
                    courseService.deleteCourse(c.getId());
                }
            }
        } else if (role != null && role == 3) {
            // 学生：清理选课关系、提交记录
            studentCourseMapper.delete(new LambdaQueryWrapper<StudentCourse>()
                    .eq(StudentCourse::getStudentId, userId));
            submissionMapper.delete(new LambdaQueryWrapper<Submission>()
                    .eq(Submission::getStudentId, userId));
        }

        // 通用：删除该用户收到的消息
        messageMapper.delete(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId));

        // 2) 删除用户本身
        return this.removeById(userId);
    }

    @Override
    public List<User> searchUsers(String keyword, Integer role) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .like(keyword != null, User::getUsername, keyword)
                .or()
                .like(keyword != null, User::getRealName, keyword);
        
        if (role != null) {
            queryWrapper.eq(User::getRole, role);
        }
        
        return this.list(queryWrapper);
    }
    
    @Override
    public User getAdmin() {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getRole, 1));
    }
    
    @Override
    public int getStudentCourseCount(String studentId) {
        return this.baseMapper.getStudentCourseCount(studentId);
    }
    
    @Override
    public int getTeacherCourseCount(String teacherId) {
        return this.baseMapper.getTeacherCourseCount(teacherId);
    }
    
    @Override
    public int countStudents() {
        return Math.toIntExact(this.count(new LambdaQueryWrapper<User>().eq(User::getRole, 3)));
    }
    
    @Override
    public int countTeachers() {
        return Math.toIntExact(this.count(new LambdaQueryWrapper<User>().eq(User::getRole, 2)));
    }
    
    @Override
    public int countAdmins() {
        return Math.toIntExact(this.count(new LambdaQueryWrapper<User>().eq(User::getRole, 1)));
    }
    
    @Override
    public int migrateUsernameFormat() {
        int migratedCount = 0;
        
        // 为学生用户生成格式化ID作为主键（保持username不变）
        migratedCount += migrateUsersByRole(3, "stu");
        
        // 为教师用户生成格式化ID作为主键（保持username不变）
        migratedCount += migrateUsersByRole(2, "tea");
        
        // 为管理员用户生成格式化ID作为主键（保持username不变）
        migratedCount += migrateUsersByRole(1, "adm");
        
        return migratedCount;
    }
    
    /**
     * 为指定角色的用户生成格式化ID作为主键（保持username不变）
     * @param role 角色：1-管理员，2-教师，3-学生
     * @param prefix 前缀：adm, tea 或 stu
     * @return 迁移的用户数量
     */
    private int migrateUsersByRole(Integer role, String prefix) {
        int migratedCount = 0;
        
        // 查找所有该角色的用户（按创建时间和ID排序）
        List<User> allUsers = this.list(new LambdaQueryWrapper<User>()
                .eq(User::getRole, role)
                .orderByAsc(User::getCreateTime));
        
        // 找出已存在的格式化ID的最大编号
        int maxFiveDigitNumber = 0;
        List<User> usersToMigrate = new java.util.ArrayList<>();
        
        for (User user : allUsers) {
            String id = user.getId();
            // 如果ID已经是格式化ID格式，检查并记录最大编号
            if (id != null && id.startsWith(prefix) && id.length() == prefix.length() + 5) {
                try {
                    String numberStr = id.substring(prefix.length());
                    int number = Integer.parseInt(numberStr);
                    if (number > maxFiveDigitNumber) {
                        maxFiveDigitNumber = number;
                    }
                } catch (NumberFormatException e) {
                    // 格式不正确，需要重新生成
                    usersToMigrate.add(user);
                }
            } else {
                // ID不是格式化ID格式，需要迁移
                usersToMigrate.add(user);
            }
        }
        
        // 从最大编号+1开始分配新编号
        int nextNumber = maxFiveDigitNumber + 1;
        for (User user : usersToMigrate) {
            String newFormattedId = String.format("%s%05d", prefix, nextNumber);
            
            // 检查新ID是否已存在（防止冲突）
            User existingUser = this.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getId, newFormattedId));
            
            if (existingUser == null) {
                // 更新ID为格式化ID，保持username不变
                String oldId = user.getId() != null ? user.getId() : "(null)";
                String oldUsername = user.getUsername() != null ? user.getUsername() : "(null)";
                
                // 使用SQL直接更新主键（因为updateById无法更新主键）
                // 先更新所有外键关联的表
                this.baseMapper.updateTeacherIdInCourse(oldId, newFormattedId);
                this.baseMapper.updateStudentIdInStudentCourse(oldId, newFormattedId);
                this.baseMapper.updateStudentIdInSubmission(oldId, newFormattedId);
                this.baseMapper.updateUserIdInMessages(oldId, newFormattedId);
                
                // 然后更新用户表的主键
                this.baseMapper.updateUserId(oldId, newFormattedId);
                
                System.out.println("迁移用户ID: " + oldId + " (" + oldUsername + ") → " + newFormattedId);
                migratedCount++;
                nextNumber++;
            } else {
                // 如果新ID已存在，跳过这个编号
                nextNumber++;
            }
        }
        
        return migratedCount;
    }
}