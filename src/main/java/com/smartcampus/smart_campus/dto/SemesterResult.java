package com.smartcampus.smart_campus.dto;

import java.util.List;

public record SemesterResult(int semester, List<SubjectResult> subjects, double sgpa) {}