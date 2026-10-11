package com.smartcampus.smart_campus.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MarkEntry(
        @NotNull(message = "Student ID is required") Long studentId,
        @NotNull(message = "Internal marks are required")
        @Min(value = 0, message = "Internal marks must be between 0 and 40")
        @Max(value = 40, message = "Internal marks must be between 0 and 40") Integer internalMarks,
        @NotNull(message = "External marks are required")
        @Min(value = 0, message = "External marks must be between 0 and 60")
        @Max(value = 60, message = "External marks must be between 0 and 60") Integer externalMarks
) {}