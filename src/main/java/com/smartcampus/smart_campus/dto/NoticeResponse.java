package com.smartcampus.smart_campus.dto;

import com.smartcampus.smart_campus.entity.NoticeAudience;
import com.smartcampus.smart_campus.entity.Role;

import java.time.LocalDateTime;

public record NoticeResponse(
        Long id,
        String title,
        String content,
        NoticeAudience audience,
        String postedBy,
        Role postedByRole,
        LocalDateTime createdAt
) {}