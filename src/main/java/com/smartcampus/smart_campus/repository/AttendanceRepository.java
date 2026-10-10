package com.smartcampus.smart_campus.repository;

import com.smartcampus.smart_campus.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findBySubjectIdAndStudentIdAndAttendanceDate(
            Long subjectId, Long studentId, LocalDate attendanceDate);

    List<Attendance> findBySubjectId(Long subjectId);

    List<Attendance> findByStudentId(Long studentId);

    List<Attendance> findBySubjectIdAndStudentIdOrderByAttendanceDateAsc(Long subjectId, Long studentId);
}