package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.FacultyResponse;
import com.smartcampus.smart_campus.dto.SubjectResponse;
import com.smartcampus.smart_campus.service.FacultyService;
import com.smartcampus.smart_campus.service.SubjectService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    private final FacultyService facultyService;
    private final SubjectService subjectService;

    public FacultyController(FacultyService facultyService, SubjectService subjectService) {
        this.facultyService = facultyService;
        this.subjectService = subjectService;
    }

    @GetMapping("/me")
    public FacultyResponse myProfile(Authentication authentication) {
        return facultyService.getByEmail(authentication.getName());
    }

    @GetMapping("/subjects")
    public List<SubjectResponse> mySubjects(Authentication authentication) {
        return subjectService.getByFacultyEmail(authentication.getName());
    }
}