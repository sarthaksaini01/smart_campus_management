package com.smartcampus.smart_campus.dto;

import jakarta.validation.constraints.*;

public record SubjectRequest(
        @NotBlank(message = "Subject code is required") String code,
        @NotBlank(message = "Subject name is required") String name,
        @NotNull(message = "Semester is required")
        @Min(value = 1, message = "Semester must be at least 1") Integer semester,
        @NotNull(message = "Course ID is required") Long courseId
) {}