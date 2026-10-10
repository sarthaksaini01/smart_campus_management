package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.AttendanceRecordResponse;
import com.smartcampus.smart_campus.dto.SubjectAttendanceSummary;
import com.smartcampus.smart_campus.service.AttendanceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/attendance")
public class StudentAttendanceController {

    private final AttendanceService attendanceService;

    public StudentAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public List<SubjectAttendanceSummary> mySummary(Authentication auth) {
        return attendanceService.mySummary(auth.getName());
    }

    @GetMapping("/{subjectId}")
    public List<AttendanceRecordResponse> myRecords(@PathVariable Long subjectId, Authentication auth) {
        return attendanceService.myRecords(auth.getName(), subjectId);
    }
}