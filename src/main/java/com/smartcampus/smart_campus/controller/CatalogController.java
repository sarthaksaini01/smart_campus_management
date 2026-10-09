package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.CourseResponse;
import com.smartcampus.smart_campus.dto.SubjectResponse;
import com.smartcampus.smart_campus.service.CourseService;
import com.smartcampus.smart_campus.service.SubjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CourseService courseService;
    private final SubjectService subjectService;

    public CatalogController(CourseService courseService, SubjectService subjectService) {
        this.courseService = courseService;
        this.subjectService = subjectService;
    }

    @GetMapping("/courses")
    public List<CourseResponse> courses() {
        return courseService.getAll();
    }

    @GetMapping("/courses/{id}")
    public CourseResponse course(@PathVariable Long id) {
        return courseService.getById(id);
    }

    @GetMapping("/subjects")
    public List<SubjectResponse> subjects(@RequestParam(required = false) Long courseId) {
        return subjectService.getAll(courseId);
    }

    @GetMapping("/subjects/{id}")
    public SubjectResponse subject(@PathVariable Long id) {
        return subjectService.getById(id);
    }
}