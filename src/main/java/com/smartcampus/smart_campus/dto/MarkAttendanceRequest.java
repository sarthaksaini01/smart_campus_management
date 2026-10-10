package com.smartcampus.smart_campus.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record MarkAttendanceRequest(
        @NotNull(message = "Subject ID is required") Long subjectId,
        @NotNull(message = "Date is required") LocalDate date,
        @NotEmpty(message = "At least one record is required") @Valid List<AttendanceEntry> records
) {}