package com.smartcampus.smart_campus.dto;

public record SubjectResult(
        Long subjectId,
        String subjectCode,
        String subjectName,
        int internalMarks,
        int externalMarks,
        int total,
        String grade,
        int gradePoint,
        boolean passed
) {}