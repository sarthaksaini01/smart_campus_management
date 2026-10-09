package com.smartcampus.smart_campus.dto;

import com.smartcampus.smart_campus.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
}