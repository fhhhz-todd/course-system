package com.example.course.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.course.common.Result;
import com.example.course.entity.Submission;
import com.example.course.entity.User;
import com.example.course.entity.Assignment;
import com.example.course.entity.Course;
import com.example.course.service.SubmissionService;
import com.example.course.service.FileService;
import com.example.course.service.UserService;
import com.example.course.service.AssignmentService;
import com.example.course.service.CourseService;
import com.example.course.service.StudentCourseService;
import com.example.course.mapper.StudentCourseMapper;
import com.example.course.dto.GradeRequest;
import com.example.course.dto.GradeImportDTO;
import com.example.course.dto.GradeExportDTO;
import com.example.course.dto.GradeByAssignmentStudentRequest;
import com.example.course.service.MessageService;
import com.example.course.vo.SubmissionVO;
import com.example.course.vo.CourseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AssignmentService assignmentService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private StudentCourseService studentCourseService;
    
    @Autowired
    private StudentCourseMapper studentCourseMapper;
    
    @Autowired
    private MessageService messageService;
    
    // 获取当前用户的方法
    private User getCurrentUser(HttpServletRequest request) {
        // 在当前系统中，前端使用sessionStorage存储用户信息，
        // 但后端无法直接访问sessionStorage
        // 我们需要通过其他方式验证权限
        // 暂时通过在请求中传递用户ID和角色信息来实现
        String userIdStr = request.getHeader("X-User-Id");
        String userRoleStr = request.getHeader("X-User-Role");
        
        if (userIdStr != null && userRoleStr != null) {
            try {
                String userId = userIdStr;
                Integer userRole = Integer.parseInt(userRoleStr);
                
                // 从数据库中获取用户信息进行验证
                User user = userService.getById(userId);
                if (user != null && user.getRole().equals(userRole)) {
                    return user;
                }
            } catch (NumberFormatException e) {
                // 如果解析失败，返回null
            }
        }
        
        return null;
    }

    @PostMapping
    public Result submitAssignment(
            @RequestParam Long assignmentId,
            @RequestParam String studentId,  // 接收studentId参数
            @RequestParam(required = false) String content,
            @RequestParam(required = false) MultipartFile[] files,
            HttpServletRequest request) { // 添加HttpServletRequest参数
        
        try {
            // 验证用户ID的有效性
            User currentUser = userService.getById(studentId);
            if (currentUser == null) {
                return Result.error("用户不存在");
            }
            
            // 验证用户角色是否为学生
            if (!currentUser.getRole().equals(3)) { // 3代表学生角色
                return Result.error("只有学生可以提交作业");
            }

            // 验证作业是否存在
            Assignment assignment = assignmentService.getById(assignmentId);
            if (assignment == null) {
                return Result.error("作业不存在");
            }

            // 验证学生是否选修了包含该作业的课程
            // 获取作业所属课程
            Course course = courseService.getById(assignment.getCourseId());
            if (course == null) {
                return Result.error("作业所属课程不存在");
            }
            
            // 检查学生是否选修了该课程
            boolean isStudentEnrolled = checkStudentEnrollment(studentId, course.getId());
            if (!isStudentEnrolled) {
                return Result.error("您未选修此课程，无法提交作业");
            }

            Submission submission = new Submission();
            submission.setAssignmentId(assignmentId);
            submission.setStudentId(studentId);
            submission.setContent(content);

            // 处理文件上传
            if (files != null && files.length > 0) {
                StringBuilder filePaths = new StringBuilder();
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isEmpty()) {
                        String fileName = fileService.saveFile(files[i]);
                        if (i > 0) {
                            filePaths.append(";");
                        }
                        filePaths.append("/uploads/").append(fileName);
                    }
                }
                submission.setAttachmentPath(filePaths.toString());
            }

            submission.setCreateTime(LocalDateTime.now());
            submission.setUpdateTime(LocalDateTime.now());
            submission.setStatus(0); // 0-已提交, 1-已批阅

            submissionService.save(submission);
            
            // 发送消息给教师，通知有新作业提交
            try {
                // 获取作业信息
                Assignment notificationAssignment = assignmentService.getById(assignmentId);
                if (notificationAssignment != null) {
                    Course notificationCourse = courseService.getById(notificationAssignment.getCourseId());
                    if (notificationCourse != null) {
                        // 获取教师信息
                        User teacher = userService.getById(notificationCourse.getTeacherId());
                        if (teacher != null) {
                            String title = "新作业提交通知";
                            String messageContent = String.format("课程《%s》中有新作业提交。\n学生：%s\n作业：%s\n提交时间：%s", 
                                notificationCourse.getCourseName(), 
                                currentUser.getRealName(), 
                                notificationAssignment.getTitle(),
                                LocalDateTime.now().toString().replace("T", " "));
                            String sender = "系统通知";
                            
                            messageService.createMessage(teacher.getId(), title, messageContent, sender);
                        }
                    }
                }
            } catch (Exception e) {
                // 如果发送消息失败，不影响作业提交结果
                e.printStackTrace();
            }
            
            return Result.success("作业提交成功");
        } catch (Exception e) {
            e.printStackTrace(); // 添加异常打印，便于调试
            return Result.error("作业提交失败: " + e.getMessage());
        }
    }

    // 检查学生是否选修了指定课程
    private boolean checkStudentEnrollment(String studentId, Long courseId) {
        try {
            // 调用StudentCourseService检查学生是否选修了课程
            // 获取学生的所有课程
            List<CourseVO> studentCourses = studentCourseService.getCoursesByStudentId(studentId);
            
            // 检查是否包含当前课程
            if (studentCourses != null) {
                for (CourseVO course : studentCourses) {
                    if (course.getId().equals(courseId)) {
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            // 如果获取课程列表失败，出于安全考虑，不允许提交
            e.printStackTrace(); // 添加异常打印，便于调试
            return false;
        }
    }

    @GetMapping("/student/{studentId}")
    public Result getSubmissionsByStudent(@PathVariable String studentId, HttpServletRequest request) {
        // 验证权限 - 只有管理员、教师和学生本人可以查看
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        // 学生只能查看自己的提交，教师和管理员可以查看指定学生的提交
        if (currentUser.getRole().equals(3)) { // 学生
            if (!currentUser.getId().equals(studentId)) {
                return Result.error("权限不足");
            }
        } else if (!currentUser.getRole().equals(1) && !currentUser.getRole().equals(2)) { // 非管理员和非教师
            return Result.error("权限不足");
        }
        
        User targetUser = userService.getById(studentId);
        if (targetUser == null) {
            return Result.error("用户不存在");
        }
        
        List<Submission> submissions = submissionService.list(
            Wrappers.<Submission>lambdaQuery()
                .eq(Submission::getStudentId, studentId)
                .orderByDesc(Submission::getCreateTime)
        );
        return Result.success(submissions);
    }

    @GetMapping("/assignment/{assignmentId}")
    public Result getSubmissionsByAssignment(@PathVariable Long assignmentId, HttpServletRequest request) {
        // 验证权限 - 只有管理员和教师可以查看作业的所有提交
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        if (!currentUser.getRole().equals(1) && !currentUser.getRole().equals(2)) { // 不是管理员或教师
            return Result.error("权限不足");
        }
        
        // 如果是教师，需要验证该作业是否属于他教授的课程
        if (currentUser.getRole().equals(2)) { // 教师
            Assignment assignment = assignmentService.getById(assignmentId);
            if (assignment != null && !assignment.getCourseId().equals(currentUser.getId())) {
                Course course = courseService.getById(assignment.getCourseId());
                if (course != null && !course.getTeacherId().equals(currentUser.getId())) {
                    return Result.error("权限不足，无法查看此作业的提交");
                }
            } else if (assignment == null) {
                return Result.error("作业不存在");
            }
        }
        
        // 获取作业信息
        Assignment assignment = assignmentService.getById(assignmentId);
        if (assignment == null) {
            return Result.error("作业不存在");
        }
        
        // 获取课程的所有学生
        List<String> studentIds = studentCourseMapper.selectStudentIdsByCourseId(assignment.getCourseId());
        List<User> courseStudents = new ArrayList<>();
        if (studentIds != null && !studentIds.isEmpty()) {
            courseStudents = userService.listByIds(studentIds).stream()
                .filter(u -> u.getRole() != null && u.getRole() == 3) // 确保是学生
                .collect(Collectors.toList());
        }
        
        // 获取所有提交记录
        List<Submission> submissions = submissionService.list(
            Wrappers.<Submission>lambdaQuery()
                .eq(Submission::getAssignmentId, assignmentId)
        );
        
        // 创建学生ID到提交记录的映射
        java.util.Map<String, Submission> submissionMap = submissions.stream()
            .collect(Collectors.toMap(Submission::getStudentId, s -> s, (s1, s2) -> s1));
        
        // 构建包含所有学生的VO列表（包括未提交的）
        List<SubmissionVO> submissionVOs = new ArrayList<>();
        
        // 分类：已提交未批改、已提交已批改、未提交
        List<SubmissionVO> ungradedList = new ArrayList<>();
        List<SubmissionVO> gradedList = new ArrayList<>();
        List<SubmissionVO> notSubmittedList = new ArrayList<>();
        
        for (User student : courseStudents) {
            Submission submission = submissionMap.get(student.getId());
            SubmissionVO vo;
            
            if (submission != null) {
                // 有提交记录
                vo = SubmissionVO.fromEntity(submission);
                vo.setStudentName(student.getRealName());
                vo.setAssignmentTitle(assignment.getTitle());
                Course course = courseService.getById(assignment.getCourseId());
                if (course != null) {
                    vo.setCourseName(course.getCourseName());
                }
                
                // 按状态分类
                if (submission.getStatus() == null || submission.getStatus() == 0) {
                    // 已提交未批改
                    ungradedList.add(vo);
                } else {
                    // 已提交已批改
                    gradedList.add(vo);
                }
            } else {
                // 未提交，创建一个虚拟的VO
                vo = new SubmissionVO();
                vo.setId(null); // 没有提交ID
                vo.setAssignmentId(assignmentId);
                vo.setStudentId(student.getId());
                vo.setStudentName(student.getRealName());
                vo.setAssignmentTitle(assignment.getTitle());
                Course course = courseService.getById(assignment.getCourseId());
                if (course != null) {
                    vo.setCourseName(course.getCourseName());
                }
                vo.setContent("");
                vo.setAttachmentPath("");
                vo.setScore(null);
                vo.setComment(null);
                vo.setStatus(null); // 未提交状态
                vo.setCreateTime(null);
                vo.setUpdateTime(null);
                notSubmittedList.add(vo);
            }
        }
        
        // 按要求的顺序合并：已提交未批改 > 已提交已批改 > 未提交
        submissionVOs.addAll(ungradedList);
        submissionVOs.addAll(gradedList);
        submissionVOs.addAll(notSubmittedList);
        
        return Result.success(submissionVOs);
    }

    @PutMapping("/{id}")
    public Result updateSubmission(@PathVariable Long id, @RequestBody Submission submission, HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        Submission existingSubmission = submissionService.getById(id);
        if (existingSubmission == null) {
            return Result.error("提交记录不存在");
        }
        
        // 如果是学生，只能更新自己的提交记录，且必须是未批阅状态
        if (currentUser.getRole().equals(3)) { // 学生
            if (!existingSubmission.getStudentId().equals(currentUser.getId())) {
                return Result.error("权限不足，只能修改自己的提交记录");
            }
            if (existingSubmission.getStatus() != null && existingSubmission.getStatus().equals(1)) {
                return Result.error("已批阅的作业不能修改");
            }
            // 学生只能更新内容和附件路径，不能更新分数和评语
            existingSubmission.setContent(submission.getContent());
            existingSubmission.setAttachmentPath(submission.getAttachmentPath());
            existingSubmission.setUpdateTime(LocalDateTime.now());
            submissionService.updateById(existingSubmission);
            return Result.success("更新成功");
        } else if (currentUser.getRole().equals(2)) { // 教师
            // 如果是教师，需要验证该提交是否属于他教授的课程
            Assignment assignment = assignmentService.getById(existingSubmission.getAssignmentId());
            if (assignment != null) {
                Course course = courseService.getById(assignment.getCourseId());
                if (course != null && !course.getTeacherId().equals(currentUser.getId())) {
                    return Result.error("权限不足，无法更新此提交");
                }
            } else {
                return Result.error("关联的作业不存在");
            }
        } else if (!currentUser.getRole().equals(1)) { // 不是管理员
            return Result.error("权限不足");
        }
        
        submission.setId(id);
        submission.setUpdateTime(LocalDateTime.now());
        submissionService.updateById(submission);
        return Result.success("更新成功");
    }
    
    // 学生更新提交（支持文件上传）
    @PutMapping("/{id}/student")
    public Result updateSubmissionByStudent(@PathVariable Long id,
                                           @RequestParam(required = false) String content,
                                           @RequestParam(required = false) MultipartFile[] files,
                                           HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            if (!currentUser.getRole().equals(3)) {
                return Result.error("只有学生可以访问此接口");
            }
            
            Submission existingSubmission = submissionService.getById(id);
            if (existingSubmission == null) {
                return Result.error("提交记录不存在");
            }
            
            if (!existingSubmission.getStudentId().equals(currentUser.getId())) {
                return Result.error("权限不足，只能修改自己的提交记录");
            }
            
            // 更新内容
            if (content != null) {
                existingSubmission.setContent(content);
            }
            
            // 处理文件上传
            if (files != null && files.length > 0) {
                StringBuilder filePaths = new StringBuilder();
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isEmpty()) {
                        String fileName = fileService.saveFile(files[i]);
                        if (i > 0) {
                            filePaths.append(";");
                        }
                        filePaths.append("/uploads/").append(fileName);
                    }
                }
                existingSubmission.setAttachmentPath(filePaths.toString());
            }
            
            existingSubmission.setUpdateTime(LocalDateTime.now());
            submissionService.updateById(existingSubmission);
            return Result.success("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @GetMapping
    public Result getAllSubmissions(HttpServletRequest request) {
        // 需要验证用户权限 - 只有管理员和教师可以查看
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        List<Submission> submissions;
        
        // 如果是教师，只能查看自己课程的提交记录
        if (currentUser.getRole().equals(2)) { // 教师角色
            // 获取教师教授的所有课程ID
            List<Course> courses = courseService.list(Wrappers.<Course>lambdaQuery()
                .eq(Course::getTeacherId, currentUser.getId()));
            List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
            
            if (courseIds.isEmpty()) {
                // 如果教师没有课程，返回空列表
                return Result.success(new ArrayList<>());
            }
            
            // 获取这些课程中的所有作业ID
            List<Assignment> assignments = assignmentService.list(Wrappers.<Assignment>lambdaQuery()
                .in(Assignment::getCourseId, courseIds));
            List<Long> assignmentIds = assignments.stream().map(Assignment::getId).collect(Collectors.toList());
            
            if (assignmentIds.isEmpty()) {
                // 如果课程没有作业，返回空列表
                return Result.success(new ArrayList<>());
            }
            
            // 获取这些作业的所有提交记录
            submissions = submissionService.list(Wrappers.<Submission>lambdaQuery()
                .in(Submission::getAssignmentId, assignmentIds)
                .orderByDesc(Submission::getCreateTime));
        } else if (currentUser.getRole().equals(1)) { // 管理员角色
            submissions = submissionService.list(
                Wrappers.<Submission>lambdaQuery()
                    .orderByDesc(Submission::getCreateTime)
            );
        } else {
            return Result.error("权限不足");
        }
        
        // 转换为包含关联信息的VO
        List<SubmissionVO> submissionVOs = submissions.stream().map(sub -> {
            SubmissionVO vo = SubmissionVO.fromEntity(sub);
            
            // 获取关联的作业信息
            Assignment assignment = assignmentService.getById(sub.getAssignmentId());
            if (assignment != null) {
                vo.setAssignmentTitle(assignment.getTitle());
            }
            
            // 获取关联的学生信息
            User student = userService.getById(sub.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getRealName());
            }
            
            // 获取关联的课程信息（通过作业关联到课程）
            if (assignment != null) {
                Course course = courseService.getById(assignment.getCourseId());
                if (course != null) {
                    vo.setCourseName(course.getCourseName());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        return Result.success(submissionVOs);
    }
    
    @PutMapping("/{id}/grade")
    public Result gradeSubmission(@PathVariable Long id, 
                                  @RequestBody GradeRequest gradeRequest,
                                  HttpServletRequest request) {
        // 验证权限 - 只有教师和管理员可以批改作业
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        if (!currentUser.getRole().equals(1) && !currentUser.getRole().equals(2)) { // 不是管理员或教师
            return Result.error("权限不足");
        }
        
        Submission submission = submissionService.getById(id);
        if (submission == null) {
            return Result.error("提交记录不存在");
        }
        
        // 如果是教师，需要验证该提交是否属于他教授的课程
        if (currentUser.getRole().equals(2)) { // 教师
            Assignment assignment = assignmentService.getById(submission.getAssignmentId());
            if (assignment != null) {
                Course course = courseService.getById(assignment.getCourseId());
                if (course != null && !course.getTeacherId().equals(currentUser.getId())) {
                    return Result.error("权限不足，无法批改此作业");
                }
            } else {
                return Result.error("关联的作业不存在");
            }
        }

        submission.setScore(gradeRequest.getScore());
        submission.setComment(gradeRequest.getComment());
        submission.setStatus(1); // 已批阅
        submission.setUpdateTime(LocalDateTime.now());
        
        submissionService.updateById(submission);
        
        // 发送消息给学生，通知作业已批改
        try {
            // 获取学生信息
            User notificationStudent = userService.getById(submission.getStudentId());
            if (notificationStudent != null) {
                // 获取作业和课程信息
                Assignment notificationAssignment = assignmentService.getById(submission.getAssignmentId());
                Course notificationCourse = null;
                if (notificationAssignment != null) {
                    notificationCourse = courseService.getById(notificationAssignment.getCourseId());
                }
                
                String title = "作业批改通知";
                String messageContent = String.format("您提交的作业《%s》已在课程《%s》中被批改。\n得分：%s\n评语：%s\n批改时间：%s", 
                    notificationAssignment != null ? notificationAssignment.getTitle() : "未知作业", 
                    notificationCourse != null ? notificationCourse.getCourseName() : "未知课程", 
                    gradeRequest.getScore() != null ? gradeRequest.getScore().toString() : "未评分", 
                    gradeRequest.getComment() != null ? gradeRequest.getComment() : "无评语",
                    LocalDateTime.now().toString().replace("T", " "));
                String sender = "系统通知";
                
                messageService.createMessage(notificationStudent.getId(), title, messageContent, sender);
            }
        } catch (Exception e) {
            // 如果发送消息失败，不影响批改结果
            e.printStackTrace();
        }
        
        return Result.success("批改成功");
    }
    
    /**
     * 为未提交的学生打分（通过assignmentId和studentId，如果不存在提交记录则创建）
     */
    @PostMapping("/grade-by-assignment-student")
    public Result gradeSubmissionByAssignmentAndStudent(
            @RequestBody GradeByAssignmentStudentRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 验证权限
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            if (!currentUser.getRole().equals(1) && !currentUser.getRole().equals(2)) {
                return Result.error("权限不足");
            }
            
            // 验证作业是否存在
            Assignment assignment = assignmentService.getById(request.getAssignmentId());
            if (assignment == null) {
                return Result.error("作业不存在");
            }
            
            // 如果是教师，验证权限
            if (currentUser.getRole().equals(2)) {
                Course course = courseService.getById(assignment.getCourseId());
                if (course == null || !course.getTeacherId().equals(currentUser.getId())) {
                    return Result.error("权限不足，无法批改此作业");
                }
            }
            
            // 验证学生是否存在且属于该课程
            User student = userService.getById(request.getStudentId());
            if (student == null) {
                return Result.error("学生不存在");
            }
            
            if (student.getRole() == null || !student.getRole().equals(3)) {
                return Result.error("该用户不是学生");
            }
            
            // 验证学生是否属于该课程
            List<String> courseStudentIds = studentCourseMapper.selectStudentIdsByCourseId(assignment.getCourseId());
            if (!courseStudentIds.contains(request.getStudentId())) {
                return Result.error("该学生不属于此课程");
            }
            
            // 查找或创建提交记录
            Submission submission = submissionService.getOne(
                Wrappers.<Submission>lambdaQuery()
                    .eq(Submission::getAssignmentId, request.getAssignmentId())
                    .eq(Submission::getStudentId, request.getStudentId())
                    .last("LIMIT 1")
            );
            
            if (submission == null) {
                // 创建新的提交记录（仅用于记录成绩）
                submission = new Submission();
                submission.setAssignmentId(request.getAssignmentId());
                submission.setStudentId(request.getStudentId());
                submission.setContent(""); // 未提交，内容为空
                submission.setAttachmentPath(""); // 未提交，附件路径为空
                submission.setCreateTime(LocalDateTime.now());
                submissionService.save(submission);
            }
            
            // 更新成绩
            submission.setScore(request.getScore());
            if (request.getComment() != null) {
                submission.setComment(request.getComment());
            }
            submission.setStatus(1); // 已批阅
            submission.setUpdateTime(LocalDateTime.now());
            
            submissionService.updateById(submission);
            
            // 发送消息给学生，通知作业已批改
            try {
                String title = "作业批改通知";
                Course course = courseService.getById(assignment.getCourseId());
                String messageContent = String.format("您的作业《%s》已在课程《%s》中被批改。\n得分：%s\n评语：%s\n批改时间：%s", 
                    assignment.getTitle(), 
                    course != null ? course.getCourseName() : "未知课程", 
                    request.getScore() != null ? request.getScore().toString() : "未评分", 
                    request.getComment() != null ? request.getComment() : "无评语",
                    LocalDateTime.now().toString().replace("T", " "));
                String sender = "系统通知";
                
                messageService.createMessage(student.getId(), title, messageContent, sender);
            } catch (Exception e) {
                // 如果发送消息失败，不影响批改结果
                e.printStackTrace();
            }
            
            return Result.success("批改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批改失败: " + e.getMessage());
        }
    }

    @GetMapping("/assignment/{assignmentId}/student/{studentId}")
    public Result getSubmissionByAssignmentAndStudent(
            @PathVariable Long assignmentId, 
            @PathVariable String studentId, HttpServletRequest request) {
        try {
            // 验证权限 - 只有管理员、教师和提交作业的学生本人可以查看
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            // 学生只能查看自己的提交，教师只能查看自己课程的提交，管理员可以查看所有
            if (currentUser.getRole().equals(3)) { // 学生
                if (!currentUser.getId().equals(studentId)) {
                    return Result.error("权限不足");
                }
            } else if (currentUser.getRole().equals(2)) { // 教师
                Assignment checkAssignment = assignmentService.getById(assignmentId);
                if (checkAssignment != null) {
                    Course checkCourse = courseService.getById(checkAssignment.getCourseId());
                    if (checkCourse != null && !checkCourse.getTeacherId().equals(currentUser.getId())) {
                        return Result.error("权限不足，无法查看此提交");
                    }
                } else {
                    return Result.error("作业不存在");
                }
            } else if (!currentUser.getRole().equals(1)) { // 非管理员、非教师、非学生本人
                return Result.error("权限不足");
            }
            
            List<Submission> submissions = submissionService.list(
                Wrappers.<Submission>lambdaQuery()
                    .eq(Submission::getAssignmentId, assignmentId)
                    .eq(Submission::getStudentId, studentId)
                    .orderByDesc(Submission::getCreateTime)
                    .last("LIMIT 1") // 只获取最新的一条提交记录
            );
            
            if (submissions != null && !submissions.isEmpty()) {
                Submission submission = submissions.get(0);
                SubmissionVO vo = SubmissionVO.fromEntity(submission);
                
                // 获取关联的作业信息
                Assignment voAssignment = assignmentService.getById(submission.getAssignmentId());
                if (voAssignment != null) {
                    vo.setAssignmentTitle(voAssignment.getTitle());
                }
                
                // 获取关联的学生信息
                User voStudent = userService.getById(submission.getStudentId());
                if (voStudent != null) {
                    vo.setStudentName(voStudent.getRealName());
                }
                
                // 获取关联的课程信息
                if (voAssignment != null) {
                    Course voCourse = courseService.getById(voAssignment.getCourseId());
                    if (voCourse != null) {
                        vo.setCourseName(voCourse.getCourseName());
                    }
                }
                
                return Result.success(vo);
            } else {
                return Result.error("未找到提交记录"); // 返回错误表示未提交
            }
        } catch (Exception e) {
            e.printStackTrace(); // 添加异常打印，便于调试
            return Result.error("获取提交记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result getSubmissionById(@PathVariable Long id, HttpServletRequest request) {
        // 验证权限 - 只有管理员、教师和提交作业的学生可以查看
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        Submission submission = submissionService.getById(id);
        if (submission == null) {  // 修复：使用submission而不是subscription
            return Result.error("提交记录不存在");
        }
        
        // 验证权限 - 管理员可以查看所有提交
        if (currentUser.getRole().equals(1)) { // 管理员
            // 管理员可以查看所有提交记录
        } else if (currentUser.getRole().equals(2)) { // 教师
            // 教师只能查看自己课程的提交记录
            Assignment permissionAssignment = assignmentService.getById(submission.getAssignmentId());
            if (permissionAssignment != null) {
                Course course = courseService.getById(permissionAssignment.getCourseId());
                if (course != null && !course.getTeacherId().equals(currentUser.getId())) {
                    return Result.error("权限不足，无法查看此提交");
                }
            } else {
                return Result.error("关联的作业不存在");
            }
        } else if (currentUser.getRole().equals(3)) { // 学生
            // 学生只能查看自己的提交记录
            if (!submission.getStudentId().equals(currentUser.getId())) {
                return Result.error("权限不足，无法查看此提交");
            }
        } else {
            return Result.error("权限不足");
        }
        
        SubmissionVO vo = SubmissionVO.fromEntity(submission);
        
        // 获取关联的作业信息
        Assignment assignment = assignmentService.getById(submission.getAssignmentId());  // 修复：使用submission而不是subscription
        if (assignment != null) {
            vo.setAssignmentTitle(assignment.getTitle());
        }
        
        // 获取关联的学生信息
        User infoStudent = userService.getById(submission.getStudentId());  // 修复：使用submission而不是subscription
        if (infoStudent != null) {
            vo.setStudentName(infoStudent.getRealName());
        }
        
        // 获取关联的课程信息
        if (assignment != null) {
            Course infoCourse = courseService.getById(assignment.getCourseId());
            if (infoCourse != null) {
                vo.setCourseName(infoCourse.getCourseName());
            }
        }
        
        return Result.success(vo);
    }
    
    @DeleteMapping("/{id}")
    public Result deleteSubmission(@PathVariable Long id, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            Submission submission = submissionService.getById(id);
            if (submission == null) {
                return Result.error("提交记录不存在");
            }
            
            // 如果是学生，只能删除自己的提交记录（不再限制是否已批阅）
            if (currentUser.getRole().equals(3)) { // 学生
                if (!submission.getStudentId().equals(currentUser.getId())) {
                    return Result.error("权限不足，只能删除自己的提交记录");
                }
            } else if (currentUser.getRole().equals(2)) { // 教师
                // 如果是教师，需要验证该提交是否属于他教授的课程
                Assignment deleteAssignment = assignmentService.getById(submission.getAssignmentId());
                if (deleteAssignment != null) {
                    Course deleteCourse = courseService.getById(deleteAssignment.getCourseId());
                    if (deleteCourse != null && !deleteCourse.getTeacherId().equals(currentUser.getId())) {
                        return Result.error("权限不足，无法删除此提交");
                    }
                } else {
                    return Result.error("关联的作业不存在");
                }
            } else if (!currentUser.getRole().equals(1)) { // 不是管理员、教师或学生
                return Result.error("权限不足");
            }
            
            boolean result = submissionService.removeById(id);
            if (result) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败，提交记录不存在");
            }
        } catch (Exception e) {
            e.printStackTrace(); // 添加异常打印，便于调试
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    // 学生回复批阅
    @PutMapping("/{id}/reply")
    public Result replyToGrade(@PathVariable Long id, @RequestParam String reply, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            if (!currentUser.getRole().equals(3)) {
                return Result.error("只有学生可以回复批阅");
            }
            
            Submission submission = submissionService.getById(id);
            if (submission == null) {
                return Result.error("提交记录不存在");
            }
            
            if (!submission.getStudentId().equals(currentUser.getId())) {
                return Result.error("权限不足，只能回复自己的提交记录");
            }
            
            submission.setReply(reply);
            submission.setUpdateTime(LocalDateTime.now());
            submissionService.updateById(submission);
            
            return Result.success("回复成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("回复失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传成绩（Excel）
     */
    @PostMapping("/assignment/{assignmentId}/upload-grades")
    public Result<String> uploadGrades(@PathVariable Long assignmentId,
                                       @RequestParam("file") MultipartFile file,
                                       HttpServletRequest request) {
        try {
            // 验证权限
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            if (!currentUser.getRole().equals(1) && !currentUser.getRole().equals(2)) {
                return Result.error("权限不足");
            }
            
            // 验证作业是否存在
            Assignment assignment = assignmentService.getById(assignmentId);
            if (assignment == null) {
                return Result.error("作业不存在");
            }
            
            // 如果是教师，验证权限
            if (currentUser.getRole().equals(2)) {
                Course course = courseService.getById(assignment.getCourseId());
                if (course == null || !course.getTeacherId().equals(currentUser.getId())) {
                    return Result.error("权限不足");
                }
            }
            
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                return Result.error("文件格式错误，请上传Excel文件（.xlsx或.xls）");
            }
            
            // 读取Excel文件（跳过第一行表头）
            List<GradeImportDTO> importDataList = new ArrayList<>();
            try {
                EasyExcel.read(file.getInputStream(), GradeImportDTO.class, new com.alibaba.excel.read.listener.ReadListener<GradeImportDTO>() {
                    @Override
                    public void invoke(GradeImportDTO data, com.alibaba.excel.context.AnalysisContext context) {
                        // 跳过空行或无效数据
                        if (data.getUserIdentifier() != null && !data.getUserIdentifier().trim().isEmpty()) {
                            importDataList.add(data);
                        }
                    }
                    
                    @Override
                    public void doAfterAllAnalysed(com.alibaba.excel.context.AnalysisContext context) {
                        // 读取完成
                    }
                }).sheet()
                    .headRowNumber(1) // 跳过第一行（表头）
                    .doRead();
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("读取Excel文件失败，请检查文件格式是否正确: " + e.getMessage());
            }
            
            if (importDataList.isEmpty()) {
                return Result.error("Excel文件中没有有效数据，请确保第一列是用户标识，第二列是分数");
            }
            
            // 获取课程的所有学生（用于验证学生是否属于该课程）
            final List<User> courseStudents;
            Course course = courseService.getById(assignment.getCourseId());
            if (course != null) {
                // 获取该课程的所有学生ID
                List<String> studentIds = studentCourseMapper.selectStudentIdsByCourseId(course.getId());
                if (studentIds != null && !studentIds.isEmpty()) {
                    courseStudents = userService.listByIds(studentIds).stream()
                        .filter(u -> u.getRole() != null && u.getRole() == 3) // 确保是学生
                        .collect(Collectors.toList());
                } else {
                    courseStudents = new ArrayList<>();
                }
            } else {
                courseStudents = new ArrayList<>();
            }
            
            // 批量更新成绩
            int successCount = 0;
            int failCount = 0;
            List<String> failMessages = new ArrayList<>();
            
            for (GradeImportDTO dto : importDataList) {
                if (dto.getUserIdentifier() == null || dto.getUserIdentifier().trim().isEmpty()) {
                    failCount++;
                    failMessages.add("第" + (successCount + failCount) + "行：用户标识为空");
                    continue;
                }
                
                if (dto.getScore() == null) {
                    failCount++;
                    failMessages.add("第" + (successCount + failCount) + "行：分数为空");
                    continue;
                }
                
                String userIdentifier = dto.getUserIdentifier().trim();
                
                // 通过用户标识查找学生（支持ID、用户名、真实姓名）
                User student = userService.getById(userIdentifier);
                
                // 2. 如果没找到，尝试按用户名查找
                if (student == null) {
                    student = userService.getOne(
                        Wrappers.<User>lambdaQuery()
                            .eq(User::getUsername, userIdentifier)
                            .eq(User::getRole, 3) // 确保是学生
                            .last("LIMIT 1")
                    );
                }
                
                // 3. 如果还没找到，尝试按真实姓名查找
                if (student == null) {
                    List<User> matchedUsers = userService.list(
                        Wrappers.<User>lambdaQuery()
                            .eq(User::getRealName, userIdentifier)
                            .eq(User::getRole, 3) // 确保是学生
                    );
                    if (matchedUsers.size() == 1) {
                        student = matchedUsers.get(0);
                    } else if (matchedUsers.size() > 1) {
                        // 如果有重名，需要进一步筛选课程学生
                        final List<User> finalCourseStudents = courseStudents;
                        student = matchedUsers.stream()
                            .filter(u -> {
                                String uId = u.getId();
                                return finalCourseStudents.stream()
                                    .anyMatch(cs -> cs.getId().equals(uId));
                            })
                            .findFirst()
                            .orElse(null);
                    }
                }
                
                if (student == null) {
                    failCount++;
                    failMessages.add("第" + (successCount + failCount) + "行：未找到用户 \"" + userIdentifier + "\"");
                    continue;
                }
                
                // 验证学生是否属于该课程
                final User finalStudent = student;
                boolean isCourseStudent = courseStudents.stream()
                    .anyMatch(cs -> cs.getId().equals(finalStudent.getId()));
                if (!isCourseStudent) {
                    failCount++;
                    failMessages.add("第" + (successCount + failCount) + "行：学生 \"" + student.getRealName() + "\" 不属于该课程");
                    continue;
                }
                
                // 查找或创建提交记录
                Submission submission = submissionService.getOne(
                    Wrappers.<Submission>lambdaQuery()
                        .eq(Submission::getAssignmentId, assignmentId)
                        .eq(Submission::getStudentId, student.getId())
                        .last("LIMIT 1")
                );
                
                if (submission == null) {
                    // 如果学生没有提交记录，创建一个新的提交记录（仅用于记录成绩）
                    submission = new Submission();
                    submission.setAssignmentId(assignmentId);
                    submission.setStudentId(student.getId());
                    submission.setContent(""); // 未提交，内容为空
                    submission.setAttachmentPath(""); // 未提交，附件路径为空
                    submission.setCreateTime(LocalDateTime.now());
                    submissionService.save(submission);
                }
                
                // 更新成绩（以最后上传的分数为准）
                submission.setScore(dto.getScore());
                if (dto.getComment() != null && !dto.getComment().trim().isEmpty()) {
                    submission.setComment(dto.getComment().trim());
                }
                submission.setStatus(1); // 已批阅
                submission.setUpdateTime(LocalDateTime.now());
                submissionService.updateById(submission);
                successCount++;
            }
            
            StringBuilder resultMessage = new StringBuilder();
            resultMessage.append("成功更新 ").append(successCount).append(" 条成绩记录");
            if (failCount > 0) {
                resultMessage.append("，失败 ").append(failCount).append(" 条");
                if (failMessages.size() <= 10) {
                    resultMessage.append("：\n").append(String.join("\n", failMessages));
                } else {
                    resultMessage.append("（前10条错误：\n").append(String.join("\n", failMessages.subList(0, 10))).append("\n...");
                }
            }
            
            return Result.success(resultMessage.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量下载成绩（Excel）
     */
    @GetMapping("/assignment/{assignmentId}/download-grades")
    public void downloadGrades(@PathVariable Long assignmentId,
                              @RequestParam(value = "studentIds", required = false) List<String> studentIds,
                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        try {
            // 验证权限
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            if (!currentUser.getRole().equals(1) && !currentUser.getRole().equals(2)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            // 验证作业是否存在
            Assignment assignment = assignmentService.getById(assignmentId);
            if (assignment == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 如果是教师，验证权限
            if (currentUser.getRole().equals(2)) {
                Course course = courseService.getById(assignment.getCourseId());
                if (course == null || !course.getTeacherId().equals(currentUser.getId())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            
            // 获取课程的所有学生
            Course course = courseService.getById(assignment.getCourseId());
            if (course == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 获取需要导出的学生ID列表
            List<String> targetStudentIds;
            if (studentIds != null && !studentIds.isEmpty()) {
                // 如果传入了学生ID列表，只导出这些学生
                targetStudentIds = studentIds;
            } else {
                // 如果没有传入，导出课程的所有学生
                targetStudentIds = studentCourseMapper.selectStudentIdsByCourseId(course.getId());
            }
            
            List<User> courseStudents = new ArrayList<>();
            if (targetStudentIds != null && !targetStudentIds.isEmpty()) {
                courseStudents = userService.listByIds(targetStudentIds).stream()
                    .filter(u -> u.getRole() != null && u.getRole() == 3) // 确保是学生
                    .collect(Collectors.toList());
            }
            
            // 获取所有提交记录
            List<Submission> submissions = submissionService.list(
                Wrappers.<Submission>lambdaQuery()
                    .eq(Submission::getAssignmentId, assignmentId)
            );
            
            // 创建学生ID到提交记录的映射
            java.util.Map<String, Submission> submissionMap = submissions.stream()
                .collect(Collectors.toMap(Submission::getStudentId, s -> s, (s1, s2) -> s1));
            
            // 转换为导出DTO（包含所有学生，包括未提交的）
            List<GradeExportDTO> exportDataList = new ArrayList<>();
            for (User student : courseStudents) {
                GradeExportDTO dto = new GradeExportDTO();
                dto.setStudentId(student.getId());
                dto.setStudentName(student.getRealName() != null ? student.getRealName() : "");
                
                // 获取该学生的提交记录和成绩
                Submission submission = submissionMap.get(student.getId());
                if (submission != null && submission.getScore() != null) {
                    dto.setScore(submission.getScore());
                } else {
                    // 未提交或未批改，成绩为空
                    dto.setScore(null);
                }
                
                exportDataList.add(dto);
            }
            
            // 按学生ID排序
            exportDataList.sort((a, b) -> {
                if (a.getStudentId() == null) return 1;
                if (b.getStudentId() == null) return -1;
                return a.getStudentId().compareTo(b.getStudentId());
            });
            
            // 设置响应头
            String fileName = URLEncoder.encode(assignment.getTitle() + "_成绩单", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 写入Excel
            EasyExcel.write(response.getOutputStream(), GradeExportDTO.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("成绩单")
                .doWrite(exportDataList);
                
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 下载单个学生的作业附件
     */
    @GetMapping("/{submissionId}/download-attachments")
    public void downloadSubmissionAttachments(@PathVariable Long submissionId,
                                             HttpServletRequest request,
                                             HttpServletResponse response) throws IOException {
        try {
            // 验证权限
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            // 获取提交记录
            Submission submission = submissionService.getById(submissionId);
            if (submission == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 验证权限
            if (currentUser.getRole().equals(3)) { // 学生只能下载自己的
                if (!submission.getStudentId().equals(currentUser.getId())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            } else if (currentUser.getRole().equals(2)) { // 教师只能下载自己课程的
                Assignment assignment = assignmentService.getById(submission.getAssignmentId());
                if (assignment != null) {
                    Course course = courseService.getById(assignment.getCourseId());
                    if (course == null || !course.getTeacherId().equals(currentUser.getId())) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                }
            }
            
            if (submission.getAttachmentPath() == null || submission.getAttachmentPath().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 获取学生信息
            User student = userService.getById(submission.getStudentId());
            String studentName = student != null ? student.getRealName() : "学生" + submission.getStudentId();
            
            // 解析附件路径（多个文件用分号分隔）
            String[] filePaths = submission.getAttachmentPath().split(";");
            
            if (filePaths.length == 1) {
                // 单个文件，直接下载
                String filePath = filePaths[0].startsWith("/") ? filePaths[0].substring(1) : filePaths[0];
                File file = new File(filePath);
                if (!file.exists()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                
                String fileName = URLEncoder.encode(studentName + "_附件", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
                response.setContentType("application/octet-stream");
                response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + getFileExtension(file.getName()));
                
                Files.copy(file.toPath(), response.getOutputStream());
            } else {
                // 多个文件，打包成ZIP
                String zipFileName = URLEncoder.encode(studentName + "_作业附件", StandardCharsets.UTF_8).replaceAll("\\+", "%20") + ".zip";
                response.setContentType("application/zip");
                response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + zipFileName);
                
                try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
                    for (String filePath : filePaths) {
                        String cleanPath = filePath.trim().startsWith("/") ? filePath.trim().substring(1) : filePath.trim();
                        File file = new File(cleanPath);
                        if (file.exists() && file.isFile()) {
                            ZipEntry entry = new ZipEntry(file.getName());
                            zos.putNextEntry(entry);
                            Files.copy(file.toPath(), zos);
                            zos.closeEntry();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 批量下载作业的所有附件（打包成ZIP）
     */
    @GetMapping("/assignment/{assignmentId}/download-all-attachments")
    public void downloadAllAttachments(@PathVariable Long assignmentId,
                                      @RequestParam(value = "submissionIds", required = false) List<Long> submissionIds,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws IOException {
        try {
            // 验证权限
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            if (!currentUser.getRole().equals(1) && !currentUser.getRole().equals(2)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            // 验证作业是否存在
            Assignment assignment = assignmentService.getById(assignmentId);
            if (assignment == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 如果是教师，验证权限
            if (currentUser.getRole().equals(2)) {
                Course course = courseService.getById(assignment.getCourseId());
                if (course == null || !course.getTeacherId().equals(currentUser.getId())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            
            // 获取需要打包的提交记录：
            // - 如果传入了 submissionIds，则只打包勾选的那些提交
            // - 如果未传入 submissionIds，则打包该作业下所有有附件的提交
            List<Submission> submissions;
            if (submissionIds != null && !submissionIds.isEmpty()) {
                submissions = submissionService.list(
                    Wrappers.<Submission>lambdaQuery()
                        .eq(Submission::getAssignmentId, assignmentId)
                        .in(Submission::getId, submissionIds)
                        .isNotNull(Submission::getAttachmentPath)
                        .ne(Submission::getAttachmentPath, "")
                        .orderByAsc(Submission::getStudentId)
                );
            } else {
                submissions = submissionService.list(
                    Wrappers.<Submission>lambdaQuery()
                        .eq(Submission::getAssignmentId, assignmentId)
                        .isNotNull(Submission::getAttachmentPath)
                        .ne(Submission::getAttachmentPath, "")
                        .orderByAsc(Submission::getStudentId)
                );
            }
            
            if (submissions.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 设置响应头
            String zipFileName = URLEncoder.encode(assignment.getTitle() + "_所有学生作业附件", StandardCharsets.UTF_8).replaceAll("\\+", "%20") + ".zip";
            response.setContentType("application/zip");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + zipFileName);
            
            // 创建ZIP文件
            try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
                for (Submission submission : submissions) {
                    if (submission.getAttachmentPath() == null || submission.getAttachmentPath().isEmpty()) {
                        continue;
                    }
                    
                    // 获取学生信息
                    User student = userService.getById(submission.getStudentId());
                    String studentName = student != null ? student.getRealName() : "学生" + submission.getStudentId();
                    
                    // 解析附件路径
                    String[] filePaths = submission.getAttachmentPath().split(";");
                    for (String filePath : filePaths) {
                        String cleanPath = filePath.trim().startsWith("/") ? filePath.trim().substring(1) : filePath.trim();
                        File file = new File(cleanPath);
                        if (file.exists() && file.isFile()) {
                            // 在ZIP中使用学生姓名作为目录
                            String entryName = studentName + "_" + submission.getStudentId() + "/" + file.getName();
                            ZipEntry entry = new ZipEntry(entryName);
                            zos.putNextEntry(entry);
                            Files.copy(file.toPath(), zos);
                            zos.closeEntry();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }
}