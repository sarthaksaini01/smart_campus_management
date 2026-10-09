package com.smartcampus.smart_campus.dto;

import jakarta.validation.constraints.*;

public record CourseRequest(
        @NotBlank(message = "Course code is required") String code,
        @NotBlank(message = "Course name is required") String name,
        @NotNull(message = "Total semesters is required")
        @Min(value = 1, message = "Total semesters must be between 1 and 8")
        @Max(value = 8, message = "Total semesters must be between 1 and 8") Integer totalSemesters
) {}