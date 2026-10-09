package com.smartcampus.smart_campus.repository;

import com.smartcampus.smart_campus.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCode(String code);

    boolean existsByName(String name);
}