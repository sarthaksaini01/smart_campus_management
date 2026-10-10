package com.smartcampus.smart_campus.repository;

import com.smartcampus.smart_campus.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsByCode(String code);

    boolean existsByCourseId(Long courseId);

    List<Subject> findByCourseId(Long courseId);

    List<Subject> findByFacultyUserEmail(String email);

    List<Subject> findByCourseNameAndSemester(String courseName, Integer semester);
}