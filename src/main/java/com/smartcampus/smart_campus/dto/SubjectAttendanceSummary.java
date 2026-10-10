package com.smartcampus.smart_campus.dto;

public record SubjectAttendanceSummary(
        Long subjectId,
        String subjectCode,
        String subjectName,
        long totalClasses,
        long present,
        double percentage
) {}