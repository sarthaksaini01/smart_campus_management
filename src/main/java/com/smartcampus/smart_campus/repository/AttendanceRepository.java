package com.smartcampus.smart_campus.repository;

import com.smartcampus.smart_campus.entity.Attendance;
import com.smartcampus.smart_campus.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findBySubjectIdAndStudentIdAndAttendanceDate(
            Long subjectId, Long studentId, LocalDate attendanceDate);

    List<Attendance> findBySubjectId(Long subjectId);

    List<Attendance> findByStudentId(Long studentId);

    List<Attendance> findBySubjectIdAndStudentIdOrderByAttendanceDateAsc(Long subjectId, Long studentId);

    @Query("select a.student.id, count(a), sum(case when a.status = :present then 1 else 0 end) "
            + "from Attendance a where a.student.id in :ids group by a.student.id")
    List<Object[]> totalsByStudent(@Param("ids") Collection<Long> ids,
                                   @Param("present") AttendanceStatus present);

    @Query("select a.subject.id, a.subject.code, a.subject.name, count(a), "
            + "sum(case when a.status = :present then 1 else 0 end) "
            + "from Attendance a group by a.subject.id, a.subject.code, a.subject.name")
    List<Object[]> totalsBySubject(@Param("present") AttendanceStatus present);
}