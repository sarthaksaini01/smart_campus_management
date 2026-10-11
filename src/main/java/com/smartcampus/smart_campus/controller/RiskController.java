package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.RiskReport;
import com.smartcampus.smart_campus.service.RiskService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping("/faculty/subjects/{subjectId}/at-risk")
    public List<RiskReport> subjectRisk(@PathVariable Long subjectId,
                                        @RequestParam(defaultValue = "25") int minScore,
                                        Authentication auth) {
        return riskService.forSubject(subjectId, auth.getName(), minScore);
    }

    @GetMapping("/student/risk")
    public RiskReport myRisk(Authentication auth) {
        return riskService.forStudent(auth.getName());
    }
}