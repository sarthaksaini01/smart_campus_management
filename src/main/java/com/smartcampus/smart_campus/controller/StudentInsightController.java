package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.InsightResponse;
import com.smartcampus.smart_campus.service.InsightService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentInsightController {

    private final InsightService insightService;

    public StudentInsightController(InsightService insightService) {
        this.insightService = insightService;
    }

    @GetMapping("/insights")
    public InsightResponse insights(@RequestParam(defaultValue = "en") String lang,
                                    Authentication auth) {
        return insightService.forStudent(auth.getName(), "hinglish".equalsIgnoreCase(lang));
    }
}