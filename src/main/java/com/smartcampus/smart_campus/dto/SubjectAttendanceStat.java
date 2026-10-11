package com.smartcampus.smart_campus.dto;

public record SubjectAttendanceStat(
        Long subjectId,
        String subjectCode,
        String subjectName,
        long records,
        double averageAttendance
) {}