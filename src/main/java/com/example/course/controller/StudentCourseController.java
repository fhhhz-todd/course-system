package com.example.course.controller;

import com.alibaba.excel.EasyExcel;
import com.example.course.common.Result;
import com.example.course.dto.StudentImportDTO;
import com.example.course.service.StudentCourseService;
import com.example.course.service.UserService;
import com.example.course.vo.CourseVO;
import com.example.course.vo.StudentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/student-courses")
public class StudentCourseController {

    @Autowired
    private StudentCourseService studentCourseService;
    
    @Autowired
    private UserService userService;

    /**
     * 添加单个学生到课程
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @return 添加结果
     */
    @PostMapping("/{studentId}/{courseId}")
    public Result<String> addStudentToCourse(@PathVariable String studentId, @PathVariable Long courseId) {
        try {
            boolean result = studentCourseService.addStudentToCourse(studentId, courseId);
            if (result) {
                return Result.success("学生添加到课程成功");
            } else {
                return Result.error("学生添加到课程失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量添加学生到课程
     *
     * @param studentIds 学生ID列表
     * @param courseId   课程ID
     * @return 添加结果
     */
    @PostMapping("/batch/{courseId}")
    public Result<String> addStudentsToCourse(@RequestBody List<String> studentIds, @PathVariable Long courseId) {
        try {
            boolean result = studentCourseService.addStudentsToCourse(studentIds, courseId);
            if (result) {
                return Result.success("学生批量添加到课程成功");
            } else {
                return Result.error("学生批量添加到课程失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量从课程中删除学生
     *
     * @param studentIds 学生ID列表
     * @param courseId   课程ID
     * @return 删除结果
     */
    @DeleteMapping("/batch/{courseId}")
    public Result<String> removeStudentsFromCourse(@RequestBody List<String> studentIds, @PathVariable Long courseId) {
        try {
            boolean result = studentCourseService.removeStudentsFromCourse(studentIds, courseId);
            if (result) {
                return Result.success("学生批量从课程中移除成功");
            } else {
                return Result.error("学生批量从课程中移除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从课程中删除单个学生
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @return 删除结果
     */
    @DeleteMapping("/{studentId}/{courseId}")
    public Result<String> removeStudentFromCourse(@PathVariable String studentId, @PathVariable Long courseId) {
        try {
            boolean result = studentCourseService.removeStudentFromCourse(studentId, courseId);
            if (result) {
                return Result.success("学生从课程中移除成功");
            } else {
                return Result.error("学生从课程中移除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据课程ID获取选课学生列表
     *
     * @param courseId 课程ID
     * @return 学生列表
     */
    @GetMapping("/course/{courseId}/students")
    public Result<List<StudentVO>> getStudentsByCourseId(@PathVariable Long courseId) {
        try {
            if (courseId == null || courseId <= 0) {
                return Result.error("课程ID无效");
            }
            
            List<StudentVO> students = studentCourseService.getStudentsByCourseId(courseId);
            return Result.success(students);
        } catch (Exception e) {
            return Result.error("获取学生列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据学生ID和课程ID删除选课记录（踢出学生）
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 删除结果
     */
    @DeleteMapping("/course/{courseId}/student/{studentId}")
    public Result<String> deleteStudentFromCourse(@PathVariable String studentId, @PathVariable Long courseId) {
        try {
            if (studentId == null || studentId.isEmpty()) {
                return Result.error("学生ID无效");
            }
            
            if (courseId == null || courseId <= 0) {
                return Result.error("课程ID无效");
            }
            
            boolean result = studentCourseService.deleteStudentFromCourse(studentId, courseId);
            if (result) {
                return Result.success("学生已成功踢出课程");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            return Result.error("踢出学生失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据学生ID获取已选课程列表
     *
     * @param studentId 学生ID
     * @return 课程列表
     */
    @GetMapping("/student/{studentId}/courses")
    public Result<List<CourseVO>> getCoursesByStudentId(@PathVariable String studentId) {
        try {
            if (studentId == null || studentId.isEmpty()) {
                return Result.error("学生ID无效");
            }
            
            List<CourseVO> courses = studentCourseService.getCoursesByStudentId(studentId);
            return Result.success(courses);
        } catch (Exception e) {
            return Result.error("获取课程列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量导入学生到课程（Excel）
     *
     * @param file Excel文件
     * @param courseId 课程ID
     * @return 导入结果
     */
    @PostMapping("/import/{courseId}")
    public Result<String> importStudentsFromExcel(@RequestParam("file") MultipartFile file, @PathVariable Long courseId) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                return Result.error("文件格式错误，请上传Excel文件（.xlsx或.xls）");
            }
            
            // 读取Excel文件
            List<StudentImportDTO> importDataList = new ArrayList<>();
            EasyExcel.read(file.getInputStream(), StudentImportDTO.class, new com.alibaba.excel.read.listener.ReadListener<StudentImportDTO>() {
                @Override
                public void invoke(StudentImportDTO data, com.alibaba.excel.context.AnalysisContext context) {
                    importDataList.add(data);
                }
                
                @Override
                public void doAfterAllAnalysed(com.alibaba.excel.context.AnalysisContext context) {
                    // 读取完成
                }
            }).sheet().doRead();
            
            if (importDataList.isEmpty()) {
                return Result.error("Excel文件中没有数据");
            }
            
            // 提取学生ID列表
            List<String> studentIds = new ArrayList<>();
            for (StudentImportDTO dto : importDataList) {
                if (dto.getStudentId() != null && !dto.getStudentId().isEmpty()) {
                    // 验证学生是否存在
                    if (userService.getById(dto.getStudentId()) != null) {
                        studentIds.add(dto.getStudentId());
                    }
                }
            }
            
            if (studentIds.isEmpty()) {
                return Result.error("Excel文件中没有有效的学生ID");
            }
            
            // 批量添加学生到课程
            boolean result = studentCourseService.addStudentsToCourse(studentIds, courseId);
            if (result) {
                return Result.success("成功导入 " + studentIds.size() + " 个学生");
            } else {
                return Result.error("导入部分学生失败");
            }
        } catch (IOException e) {
            return Result.error("读取Excel文件失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }
}