package com.smartcampus.smart_campus.dto;

import com.smartcampus.smart_campus.entity.AttendanceStatus;

import java.time.LocalDate;

public record AttendanceRecordResponse(LocalDate date, AttendanceStatus status) {}