package com.example.course.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }
    
    @GetMapping("/courses")
    public String courses() {
        return "forward:/courses.html";
    }
    
    @GetMapping("/assignments")
    public String assignments() {
        return "forward:/assignments.html";
    }
    
    @GetMapping("/students")
    public String students() {
        return "forward:/students.html";
    }
    
    @GetMapping("/submissions")
    public String submissions() {
        return "forward:/submissions.html";
    }
}