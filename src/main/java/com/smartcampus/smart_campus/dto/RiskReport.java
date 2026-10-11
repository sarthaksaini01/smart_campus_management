package com.smartcampus.smart_campus.dto;

import java.util.List;

public record RiskReport(
        Long studentId,
        String rollNo,
        String fullName,
        String course,
        Integer semester,
        Double attendancePercentage,
        Double averageMarks,
        int failedSubjects,
        int riskScore,
        RiskLevel riskLevel,
        List<String> reasons
) {}