package com.smartcampus.smart_campus.dto;

public record OverviewStats(
        long totalStudents,
        long totalFaculty,
        long totalCourses,
        long totalSubjects,
        long totalNotices,
        long attendanceRecords,
        double overallAttendancePercentage,
        long highRiskStudents
) {}