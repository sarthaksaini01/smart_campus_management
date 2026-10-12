package com.smartcampus.smart_campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChatRequest(
        @NotBlank(message = "Message is required")
        @Size(max = 500, message = "Message must be at most 500 characters") String message,
        @Size(max = 10, message = "History is too long") List<ChatTurn> history
) {}