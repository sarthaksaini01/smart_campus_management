package com.smartcampus.smart_campus.dto;

public record SubjectMarkRow(
        Long studentId,
        String rollNo,
        String fullName,
        Integer internalMarks,
        Integer externalMarks,
        Integer total,
        String grade,
        Integer gradePoint,
        Boolean passed
) {}