package com.smartcampus.smart_campus.repository;

import com.smartcampus.smart_campus.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByRollNo(String rollNo);

    boolean existsByRollNo(String rollNo);
}