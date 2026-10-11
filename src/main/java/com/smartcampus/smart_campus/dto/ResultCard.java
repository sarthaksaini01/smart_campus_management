package com.smartcampus.smart_campus.dto;

import java.util.List;

public record ResultCard(
        String fullName,
        String rollNo,
        String course,
        List<SemesterResult> semesters,
        double cgpa
) {}