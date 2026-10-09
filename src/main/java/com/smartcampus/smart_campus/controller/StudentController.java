package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.StudentResponse;
import com.smartcampus.smart_campus.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/me")
    public StudentResponse myProfile(Authentication authentication) {
        return studentService.getByEmail(authentication.getName());
    }
}