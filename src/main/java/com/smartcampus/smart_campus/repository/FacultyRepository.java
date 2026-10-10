package com.smartcampus.smart_campus.repository;

import com.smartcampus.smart_campus.entity.Faculty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    Optional<Faculty> findByUserEmail(String email);

    Page<Faculty> findByUserFullNameContainingIgnoreCaseOrEmployeeIdContainingIgnoreCase(
            String name, String employeeId, Pageable pageable);
}