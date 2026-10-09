package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.SubjectRequest;
import com.smartcampus.smart_campus.dto.SubjectResponse;
import com.smartcampus.smart_campus.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/subjects")
public class AdminSubjectController {

    private final SubjectService subjectService;

    public AdminSubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    public ResponseEntity<SubjectResponse> create(@Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(request));
    }

    @PutMapping("/{id}")
    public SubjectResponse update(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return subjectService.update(id, request);
    }

    @PutMapping("/{id}/faculty/{facultyId}")
    public SubjectResponse assignFaculty(@PathVariable Long id, @PathVariable Long facultyId) {
        return subjectService.assignFaculty(id, facultyId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}