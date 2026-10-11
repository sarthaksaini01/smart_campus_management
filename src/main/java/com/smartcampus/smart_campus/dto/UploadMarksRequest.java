package com.smartcampus.smart_campus.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UploadMarksRequest(
        @NotNull(message = "Subject ID is required") Long subjectId,
        @NotEmpty(message = "At least one record is required") @Valid List<MarkEntry> records
) {}