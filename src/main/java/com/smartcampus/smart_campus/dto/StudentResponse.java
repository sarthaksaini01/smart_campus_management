package com.smartcampus.smart_campus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor

public class StudentResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String rollNo;
    private String course;
    private Integer semester;

}
