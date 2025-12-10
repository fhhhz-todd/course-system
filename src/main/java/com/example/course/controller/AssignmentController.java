package com.example.course.controller;

import com.example.course.common.Result;
import com.example.course.entity.Assignment;
import com.example.course.service.AssignmentService;
import com.example.course.service.FileService;
import com.example.course.vo.AssignmentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;
    
    @Autowired
    private FileService fileService;

    /**
     * 发布作业（支持附件上传）
     *
     * @param courseId 课程ID
     * @param title 作业标题
     * @param description 作业描述
     * @param deadline 截止日期
     * @param files 附件文件
     * @return 发布结果
     */
    @PostMapping
    public Result<String> publishAssignment(
            @RequestParam Long courseId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String deadline,
            @RequestParam(required = false) MultipartFile[] files) {
        try {
            Assignment assignment = new Assignment();
            assignment.setCourseId(courseId);
            assignment.setTitle(title);
            assignment.setDescription(description);
            if (deadline != null && !deadline.isEmpty()) {
                assignment.setDeadline(java.time.LocalDate.parse(deadline).atStartOfDay());
            }
            assignment.setCreateTime(LocalDateTime.now());
            assignment.setUpdateTime(LocalDateTime.now());
            
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
                assignment.setAttachmentPath(filePaths.toString());
            }
            
            boolean result = assignmentService.publishAssignment(assignment);
            if (result) {
                return Result.success("作业发布成功");
            } else {
                return Result.error("作业发布失败");
            }
        } catch (Exception e) {
            System.err.println("发布作业时发生异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("系统异常，请联系管理员: " + e.getMessage());
        }
    }

    /**
     * 获取课程的所有作业
     *
     * @param courseId 课程ID
     * @return 作业列表
     */
    @GetMapping("/course/{courseId}")
    public Result<List<AssignmentVO>> getAssignmentsByCourseId(@PathVariable Long courseId) {
        try {
            List<AssignmentVO> assignments = assignmentService.getAssignmentsByCourseId(courseId);
            return Result.success(assignments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除作业
     *
     * @param assignmentId 作业ID
     * @return 删除结果
     */
    @DeleteMapping("/{assignmentId}")
    public Result<String> deleteAssignment(@PathVariable Long assignmentId) {
        try {
            boolean result = assignmentService.deleteAssignment(assignmentId);
            if (result) {
                return Result.success("作业删除成功");
            } else {
                return Result.error("作业删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取作业详情
     *
     * @param assignmentId 作业ID
     * @return 作业详情
     */
    @GetMapping("/{assignmentId}")
    public Result<AssignmentVO> getAssignmentDetail(@PathVariable Long assignmentId) {
        try {
            AssignmentVO assignment = assignmentService.getAssignmentDetail(assignmentId);
            return Result.success(assignment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}