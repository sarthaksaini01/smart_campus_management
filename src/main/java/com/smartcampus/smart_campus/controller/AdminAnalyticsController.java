package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.OverviewStats;
import com.smartcampus.smart_campus.dto.RiskReport;
import com.smartcampus.smart_campus.dto.SubjectAttendanceStat;
import com.smartcampus.smart_campus.dto.SubjectPerformance;
import com.smartcampus.smart_campus.service.AnalyticsService;
import com.smartcampus.smart_campus.service.RiskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;
    private final RiskService riskService;

    public AdminAnalyticsController(AnalyticsService analyticsService, RiskService riskService) {
        this.analyticsService = analyticsService;
        this.riskService = riskService;
    }

    @GetMapping("/overview")
    public OverviewStats overview() {
        return analyticsService.overview();
    }

    @GetMapping("/attendance-by-subject")
    public List<SubjectAttendanceStat> attendanceBySubject() {
        return analyticsService.attendanceBySubject();
    }

    @GetMapping("/subject-performance")
    public List<SubjectPerformance> subjectPerformance() {
        return analyticsService.subjectPerformance();
    }

    @GetMapping("/grade-distribution")
    public Map<String, Long> gradeDistribution() {
        return analyticsService.gradeDistribution();
    }

    @GetMapping("/students-by-course")
    public Map<String, Long> studentsByCourse() {
        return analyticsService.studentsByCourse();
    }

    @GetMapping("/at-risk")
    public List<RiskReport> atRisk(@RequestParam(defaultValue = "25") int minScore) {
        return riskService.allStudents(minScore);
    }
}