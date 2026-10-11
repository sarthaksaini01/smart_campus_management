package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.dto.RiskLevel;
import com.smartcampus.smart_campus.dto.RiskReport;
import com.smartcampus.smart_campus.entity.AttendanceStatus;
import com.smartcampus.smart_campus.entity.Student;
import com.smartcampus.smart_campus.entity.Subject;
import com.smartcampus.smart_campus.exception.ResourceNotFoundException;
import com.smartcampus.smart_campus.repository.AttendanceRepository;
import com.smartcampus.smart_campus.repository.MarkRepository;
import com.smartcampus.smart_campus.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RiskService {

    /** Attendance is judged only after this many recorded classes. */
    private static final int MIN_CLASSES = 3;

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarkRepository markRepository;
    private final SubjectAccessService subjectAccess;

    public RiskService(StudentRepository studentRepository,
                       AttendanceRepository attendanceRepository,
                       MarkRepository markRepository,
                       SubjectAccessService subjectAccess) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
        this.subjectAccess = subjectAccess;
    }

    @Transactional(readOnly = true)
    public List<RiskReport> allStudents(int minScore) {
        return build(studentRepository.findAll(), minScore);
    }

    @Transactional(readOnly = true)
    public List<RiskReport> forSubject(Long subjectId, String facultyEmail, int minScore) {
        Subject subject = subjectAccess.ownedSubject(subjectId, facultyEmail);
        return build(subjectAccess.studentsOf(subject), minScore);
    }

    @Transactional(readOnly = true)
    public RiskReport forStudent(String studentEmail) {
        Student student = studentRepository.findByUserEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        return build(List.of(student), 0).get(0);
    }

    private List<RiskReport> build(List<Student> students, int minScore) {
        if (students.isEmpty()) {
            return List.of();
        }
        List<Long> ids = students.stream().map(Student::getId).toList();

        Map<Long, long[]> attendance = new HashMap<>();
        for (Object[] row : attendanceRepository.totalsByStudent(ids, AttendanceStatus.PRESENT)) {
            attendance.put(num(row[0]), new long[]{num(row[1]), num(row[2])});
        }

        Map<Long, Object[]> marks = new HashMap<>();
        for (Object[] row : markRepository.totalsByStudent(ids, GradeScale.PASS_MARK)) {
            marks.put(num(row[0]), row);
        }

        return students.stream()
                .map(s -> toReport(s, attendance.get(s.getId()), marks.get(s.getId())))
                .filter(r -> r.riskScore() >= minScore)
                .sorted(Comparator.comparingInt(RiskReport::riskScore).reversed())
                .toList();
    }

    private RiskReport toReport(Student student, long[] att, Object[] mk) {
        long totalClasses = att == null ? 0 : att[0];
        long present = att == null ? 0 : att[1];
        Double attendancePct = totalClasses >= MIN_CLASSES
                ? round2(present * 100.0 / totalClasses) : null;

        Double avgMarks = null;
        long failed = 0;
        if (mk != null) {
            long count = num(mk[1]);
            avgMarks = round2(((Number) mk[2]).doubleValue());
            failed = count - num(mk[3]);
        }

        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (attendancePct != null) {
            if (attendancePct < 65) {
                score += 50;
                reasons.add("Attendance is critically low (" + attendancePct + "%)");
            } else if (attendancePct < 75) {
                score += 30;
                reasons.add("Attendance is below 75% (" + attendancePct + "%)");
            } else if (attendancePct < 85) {
                score += 15;
                reasons.add("Attendance is below 85% (" + attendancePct + "%)");
            }
        }

        if (avgMarks != null) {
            if (avgMarks < 40) {
                score += 40;
                reasons.add("Average marks are in the failing range (" + avgMarks + "/100)");
            } else if (avgMarks < 50) {
                score += 30;
                reasons.add("Average marks are below 50 (" + avgMarks + "/100)");
            } else if (avgMarks < 60) {
                score += 20;
                reasons.add("Average marks are below 60 (" + avgMarks + "/100)");
            } else if (avgMarks < 70) {
                score += 10;
                reasons.add("Average marks are below 70 (" + avgMarks + "/100)");
            }
        }

        if (failed > 0) {
            score += (int) Math.min(10, failed * 5);
            reasons.add("Failed in " + failed + " subject(s)");
        }

        RiskLevel level;
        if (attendancePct == null && avgMarks == null) {
            level = RiskLevel.INSUFFICIENT_DATA;
        } else if (score >= 50) {
            level = RiskLevel.HIGH;
        } else if (score >= 25) {
            level = RiskLevel.MEDIUM;
        } else {
            level = RiskLevel.LOW;
        }

        return new RiskReport(
                student.getId(),
                student.getRollNo(),
                student.getUser().getFullName(),
                student.getCourse(),
                student.getSemester(),
                attendancePct,
                avgMarks,
                (int) failed,
                score,
                level,
                reasons);
    }

    private static long num(Object value) {
        return ((Number) value).longValue();
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}