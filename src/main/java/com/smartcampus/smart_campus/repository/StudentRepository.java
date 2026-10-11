package com.smartcampus.smart_campus.repository;

import com.smartcampus.smart_campus.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByRollNo(String rollNo);

    Optional<Student> findByUserEmail(String email);

    boolean existsByRollNo(String rollNo);

    List<Student> findByCourseAndSemester(String course, Integer semester);

    Page<Student> findByUserFullNameContainingIgnoreCaseOrRollNoContainingIgnoreCase(
            String name, String rollNo, Pageable pageable);

    @Query("select s.course, count(s) from Student s group by s.course order by count(s) desc")
    List<Object[]> countByCourse();
}