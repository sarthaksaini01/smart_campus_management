package com.smartcampus.smart_campus.dto;



import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FacultyResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String employeeId;
    private String department;
}

