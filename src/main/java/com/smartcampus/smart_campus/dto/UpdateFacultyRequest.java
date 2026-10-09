package com.smartcampus.smart_campus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateFacultyRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Department is required")
    private String department;
}