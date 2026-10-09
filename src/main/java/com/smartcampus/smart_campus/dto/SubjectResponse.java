package com.smartcampus.smart_campus.dto;

public record SubjectResponse(
        Long id,
        String code,
        String name,
        Integer semester,
        Long courseId,
        String courseName,
        Long facultyId,
        String facultyName
) {}