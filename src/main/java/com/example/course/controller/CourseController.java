package com.example.course.controller;

import com.example.course.common.Result;
import com.example.course.entity.Course;
import com.example.course.service.CourseService;
import com.example.course.vo.CourseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 获取所有课程
     *
     * @return 课程列表
     */
    @GetMapping
    public Result<List<CourseVO>> getAllCourses() {
        try {
            List<CourseVO> courses = courseService.getAllCourses();
            return Result.success(courses);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取教师的所有课程
     *
     * @param teacherId 教师ID
     * @return 课程列表
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<CourseVO>> getCoursesByTeacherId(@PathVariable String teacherId) {
        try {
            List<CourseVO> courses = courseService.getCoursesByTeacherId(teacherId);
            return Result.success(courses);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加课程
     *
     * @param course 课程信息
     * @return 添加结果
     */
    @PostMapping
    public Result<String> addCourse(@RequestBody Course course) {
        try {
            // 验证必要字段
            if (course.getCourseCode() == null || course.getCourseCode().isEmpty()) {
                return Result.error("课程编号不能为空");
            }
            if (course.getCourseName() == null || course.getCourseName().isEmpty()) {
                return Result.error("课程名称不能为空");
            }
            if (course.getCredits() == null || course.getCredits() <= 0) {
                return Result.error("学分必须大于0");
            }
            if (course.getTeacherId() == null) {
                return Result.error("教师ID不能为空");
            }
            
            boolean result = courseService.addCourse(course);
            if (result) {
                return Result.success("课程添加成功");
            } else {
                return Result.error("课程添加失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除课程
     *
     * @param courseId 课程ID
     * @return 删除结果
     */
    @DeleteMapping("/{courseId}")
    public Result<String> deleteCourse(@PathVariable Long courseId) {
        try {
            boolean result = courseService.deleteCourse(courseId);
            if (result) {
                return Result.success("课程删除成功");
            } else {
                return Result.error("课程删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID获取单个课程详情
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    @GetMapping("/{courseId}")
    public Result<CourseVO> getCourseById(@PathVariable Long courseId) {
        try {
            if (courseId == null || courseId <= 0) {
                return Result.error("课程ID无效");
            }
            
            CourseVO course = courseService.getCourseById(courseId);
            if (course != null) {
                return Result.success(course);
            } else {
                return Result.error("课程不存在");
            }
        } catch (Exception e) {
            return Result.error("获取课程详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取课程总数
     */
    @GetMapping("/count")
    public Result<Integer> getCourseCount() {
        try {
            int count = courseService.countCourses();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取课程总数失败: " + e.getMessage());
        }
    }
}