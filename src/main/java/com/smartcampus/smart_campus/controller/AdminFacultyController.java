package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.CreateFacultyRequest;
import com.smartcampus.smart_campus.dto.FacultyResponse;
import com.smartcampus.smart_campus.dto.UpdateFacultyRequest;
import com.smartcampus.smart_campus.service.FacultyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.smartcampus.smart_campus.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
@RestController
@RequestMapping("/api/admin/faculty")
public class AdminFacultyController {

    private final FacultyService facultyService;

    public AdminFacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @PostMapping
    public ResponseEntity<FacultyResponse> create(@Valid @RequestBody CreateFacultyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facultyService.create(request));
    }

    @GetMapping
    public PageResponse<FacultyResponse> getAll(@RequestParam(defaultValue = "") String search,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by("id"));
        return facultyService.search(search.trim(), pageable);
    }

    @GetMapping("/{id}")
    public FacultyResponse getById(@PathVariable Long id) {
        return facultyService.getById(id);
    }

    @PutMapping("/{id}")
    public FacultyResponse update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateFacultyRequest request) {
        return facultyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facultyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}