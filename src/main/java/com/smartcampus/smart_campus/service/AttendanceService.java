package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.dto.*;
import com.smartcampus.smart_campus.entity.Attendance;
import com.smartcampus.smart_campus.entity.AttendanceStatus;
import com.smartcampus.smart_campus.entity.Student;
import com.smartcampus.smart_campus.entity.Subject;
import com.smartcampus.smart_campus.exception.ForbiddenException;
import com.smartcampus.smart_campus.exception.InvalidRequestException;
import com.smartcampus.smart_campus.exception.ResourceNotFoundException;
import com.smartcampus.smart_campus.repository.AttendanceRepository;
import com.smartcampus.smart_campus.repository.StudentRepository;
import com.smartcampus.smart_campus.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             SubjectRepository subjectRepository,
                             StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
    }

    // ---------- Faculty side ----------

    @Transactional(readOnly = true)
    public List<StudentResponse> studentsOfSubject(Long subjectId, String facultyEmail) {
        Subject subject = ownedSubject(subjectId, facultyEmail);
        return studentsOf(subject).stream().map(this::toStudentResponse).toList();
    }

    @Transactional
    public int mark(MarkAttendanceRequest request, String facultyEmail) {
        Subject subject = ownedSubject(request.subjectId(), facultyEmail);

        if (request.date().isAfter(LocalDate.now())) {
            throw new InvalidRequestException("Cannot mark attendance for a future date");
        }

        Map<Long, Student> eligible = studentsOf(subject).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        Set<Long> seen = new HashSet<>();

        for (AttendanceEntry entry : request.records()) {
            Student student = eligible.get(entry.studentId());
            if (student == null) {
                throw new InvalidRequestException(
                        "Student " + entry.studentId() + " is not enrolled in this subject");
            }
            if (!seen.add(entry.studentId())) {
                throw new InvalidRequestException("Duplicate entry for student " + entry.studentId());
            }

            Attendance record = attendanceRepository
                    .findBySubjectIdAndStudentIdAndAttendanceDate(subject.getId(), student.getId(), request.date())
                    .orElseGet(Attendance::new);
            record.setSubject(subject);
            record.setStudent(student);
            record.setAttendanceDate(request.date());
            record.setStatus(entry.status());
            attendanceRepository.save(record);
        }
        return request.records().size();
    }

    @Transactional(readOnly = true)
    public List<StudentAttendanceRow> subjectSummary(Long subjectId, String facultyEmail, Double below) {
        Subject subject = ownedSubject(subjectId, facultyEmail);

        Map<Long, List<Attendance>> byStudent = attendanceRepository.findBySubjectId(subjectId).stream()
                .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

        return studentsOf(subject).stream()
                .map(st -> {
                    List<Attendance> list = byStudent.getOrDefault(st.getId(), List.of());
                    long present = countPresent(list);
                    return new StudentAttendanceRow(
                            st.getId(),
                            st.getRollNo(),
                            st.getUser().getFullName(),
                            list.size(),
                            present,
                            percent(present, list.size()));
                })
                .filter(row -> below == null || row.percentage() < below)
                .toList();
    }

    // ---------- Student side ----------

    @Transactional(readOnly = true)
    public List<SubjectAttendanceSummary> mySummary(String studentEmail) {
        Student student = findStudent(studentEmail);

        Map<Long, List<Attendance>> bySubject = attendanceRepository.findByStudentId(student.getId()).stream()
                .collect(Collectors.groupingBy(a -> a.getSubject().getId()));

        return subjectRepository.findByCourseNameAndSemester(student.getCourse(), student.getSemester())
                .stream()
                .map(sub -> {
                    List<Attendance> list = bySubject.getOrDefault(sub.getId(), List.of());
                    long present = countPresent(list);
                    return new SubjectAttendanceSummary(
                            sub.getId(), sub.getCode(), sub.getName(),
                            list.size(), present, percent(present, list.size()));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> myRecords(String studentEmail, Long subjectId) {
        Student student = findStudent(studentEmail);
        return attendanceRepository
                .findBySubjectIdAndStudentIdOrderByAttendanceDateAsc(subjectId, student.getId())
                .stream()
                .map(a -> new AttendanceRecordResponse(a.getAttendanceDate(), a.getStatus()))
                .toList();
    }

    // ---------- Helpers ----------

    private Subject ownedSubject(Long subjectId, String facultyEmail) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + subjectId));
        if (subject.getFaculty() == null
                || !subject.getFaculty().getUser().getEmail().equals(facultyEmail)) {
            throw new ForbiddenException("You are not assigned to this subject");
        }
        return subject;
    }

    private List<Student> studentsOf(Subject subject) {
        return studentRepository.findByCourseAndSemester(
                subject.getCourse().getName(), subject.getSemester());
    }

    private Student findStudent(String email) {
        return studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    private long countPresent(List<Attendance> list) {
        return list.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
    }

    private double percent(long present, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(present * 10000.0 / total) / 100.0;
    }

    private StudentResponse toStudentResponse(Student s) {
        return new StudentResponse(
                s.getId(),
                s.getUser().getId(),
                s.getUser().getFullName(),
                s.getUser().getEmail(),
                s.getRollNo(),
                s.getCourse(),
                s.getSemester());
    }
}