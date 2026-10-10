package com.smartcampus.smart_campus.dto;

public record StudentAttendanceRow(
        Long studentId,
        String rollNo,
        String fullName,
        long totalClasses,
        long present,
        double percentage
) {}