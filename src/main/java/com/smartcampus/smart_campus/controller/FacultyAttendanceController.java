package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.MarkAttendanceRequest;
import com.smartcampus.smart_campus.dto.StudentAttendanceRow;
import com.smartcampus.smart_campus.dto.StudentResponse;
import com.smartcampus.smart_campus.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty")
public class FacultyAttendanceController {

    private final AttendanceService attendanceService;

    public FacultyAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/subjects/{subjectId}/students")
    public List<StudentResponse> students(@PathVariable Long subjectId, Authentication auth) {
        return attendanceService.studentsOfSubject(subjectId, auth.getName());
    }

    @PostMapping("/attendance")
    public Map<String, Object> mark(@Valid @RequestBody MarkAttendanceRequest request,
                                    Authentication auth) {
        int count = attendanceService.mark(request, auth.getName());
        return Map.of("message", "Attendance saved", "records", count);
    }

    @GetMapping("/subjects/{subjectId}/attendance/summary")
    public List<StudentAttendanceRow> summary(@PathVariable Long subjectId,
                                              @RequestParam(required = false) Double below,
                                              Authentication auth) {
        return attendanceService.subjectSummary(subjectId, auth.getName(), below);
    }
}