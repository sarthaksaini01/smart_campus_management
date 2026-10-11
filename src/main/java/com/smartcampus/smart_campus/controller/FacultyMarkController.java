package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.SubjectMarkRow;
import com.smartcampus.smart_campus.dto.UploadMarksRequest;
import com.smartcampus.smart_campus.service.MarkService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty")
public class FacultyMarkController {

    private final MarkService markService;

    public FacultyMarkController(MarkService markService) {
        this.markService = markService;
    }

    @PostMapping("/marks")
    public Map<String, Object> upload(@Valid @RequestBody UploadMarksRequest request,
                                      Authentication auth) {
        int count = markService.upload(request, auth.getName());
        return Map.of("message", "Marks saved", "records", count);
    }

    @GetMapping("/subjects/{subjectId}/marks")
    public List<SubjectMarkRow> subjectMarks(@PathVariable Long subjectId, Authentication auth) {
        return markService.subjectMarks(subjectId, auth.getName());
    }
}