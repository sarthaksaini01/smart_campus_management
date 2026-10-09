package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.FacultyResponse;
import com.smartcampus.smart_campus.service.FacultyService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping("/me")
    public FacultyResponse myProfile(Authentication authentication) {
        return facultyService.getByEmail(authentication.getName());
    }
}