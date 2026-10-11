package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.ResultCard;
import com.smartcampus.smart_campus.service.MarkService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentResultController {

    private final MarkService markService;

    public StudentResultController(MarkService markService) {
        this.markService = markService;
    }

    @GetMapping("/results")
    public ResultCard myResults(Authentication auth) {
        return markService.myResults(auth.getName());
    }
}