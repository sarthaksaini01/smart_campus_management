package com.smartcampus.smart_campus.dto;

import com.smartcampus.smart_campus.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record AttendanceEntry(
        @NotNull(message = "Student ID is required") Long studentId,
        @NotNull(message = "Status is required") AttendanceStatus status
) {}