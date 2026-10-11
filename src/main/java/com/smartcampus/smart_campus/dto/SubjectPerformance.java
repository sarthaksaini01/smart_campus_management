package com.smartcampus.smart_campus.dto;

public record SubjectPerformance(
        Long subjectId,
        String subjectCode,
        String subjectName,
        long students,
        double averageMarks,
        double passPercentage
) {}