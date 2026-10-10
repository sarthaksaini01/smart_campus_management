package com.smartcampus.smart_campus.dto;

import com.smartcampus.smart_campus.entity.NoticeAudience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoticeRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must be at most 150 characters") String title,
        @NotBlank(message = "Content is required")
        @Size(max = 2000, message = "Content must be at most 2000 characters") String content,
        @NotNull(message = "Audience is required") NoticeAudience audience
) {}