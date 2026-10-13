package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.dto.RiskLevel;
import com.smartcampus.smart_campus.dto.RiskReport;
import com.smartcampus.smart_campus.entity.AttendanceStatus;
import com.smartcampus.smart_campus.entity.Student;
import com.smartcampus.smart_campus.entity.User;
import com.smartcampus.smart_campus.exception.ResourceNotFoundException;
import com.smartcampus.smart_campus.repository.AttendanceRepository;
import com.smartcampus.smart_campus.repository.MarkRepository;
import com.smartcampus.smart_campus.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private MarkRepository markRepository;
    @Mock
    private SubjectAccessService subjectAccess;

    private RiskService riskService;

    @BeforeEach
    void setUp() {
        riskService = new RiskService(studentRepository, attendanceRepository, markRepository, subjectAccess);
    }

    private static Student student(long id, String name) {
        User user = new User();
        user.setId(id);
        user.setFullName(name);
        Student s = new Student();
        s.setId(id);
        s.setUser(user);
        s.setRollNo("R" + id);
        s.setCourse("B.Tech CSE");
        s.setSemester(5);
        return s;
    }

    /** One database row: row(studentId, totalClasses, present) or row(studentId, count, avg, passed). */
    private static Object[] row(Object... values) {
        return values;
    }

    private void attendanceRows(Object[]... rows) {
        when(attendanceRepository.totalsByStudent(anyCollection(), eq(AttendanceStatus.PRESENT)))
                .thenReturn(Arrays.asList(rows));
    }

    private void markRows(Object[]... rows) {
        when(markRepository.totalsByStudent(anyCollection(), anyInt()))
                .thenReturn(Arrays.asList(rows));
    }

    @Test
    void weakStudentIsHighRiskWithAllThreeReasons() {
        when(studentRepository.findAll()).thenReturn(List.of(student(4, "Weak")));
        attendanceRows(row(4L, 3L, 1L));
        markRows(row(4L, 2L, 30.0, 0L));

        RiskReport report = riskService.allStudents(0).get(0);

        assertEquals(100, report.riskScore());
        assertEquals(RiskLevel.HIGH, report.riskLevel());
        assertEquals(33.33, report.attendancePercentage(), 0.001);
        assertEquals(30.0, report.averageMarks(), 0.001);
        assertEquals(2, report.failedSubjects());
        assertEquals(3, report.reasons().size());
    }

    @Test
    void goodStudentIsLowRiskWithoutReasons() {
        when(studentRepository.findAll()).thenReturn(List.of(student(5, "Good")));
        attendanceRows(row(5L, 4L, 4L));
        markRows(row(5L, 2L, 85.0, 2L));

        RiskReport report = riskService.allStudents(0).get(0);

        assertEquals(0, report.riskScore());
        assertEquals(RiskLevel.LOW, report.riskLevel());
        assertTrue(report.reasons().isEmpty());
    }

    @Test
    void lowAttendanceAloneIsMediumRisk() {
        when(studentRepository.findAll()).thenReturn(List.of(student(6, "Priya")));
        attendanceRows(row(6L, 3L, 2L));

        RiskReport report = riskService.allStudents(0).get(0);

        assertEquals(66.67, report.attendancePercentage(), 0.001);
        assertEquals(30, report.riskScore());
        assertEquals(RiskLevel.MEDIUM, report.riskLevel());
        assertNull(report.averageMarks());
    }

    @Test
    void studentWithoutAnyDataHasInsufficientData() {
        when(studentRepository.findAll()).thenReturn(List.of(student(7, "New")));

        RiskReport report = riskService.allStudents(0).get(0);

        assertEquals(RiskLevel.INSUFFICIENT_DATA, report.riskLevel());
        assertNull(report.attendancePercentage());
    }

    @Test
    void attendanceIsNotJudgedBeforeThreeClasses() {
        when(studentRepository.findAll()).thenReturn(List.of(student(8, "Early")));
        attendanceRows(row(8L, 2L, 0L));

        RiskReport report = riskService.allStudents(0).get(0);

        assertNull(report.attendancePercentage());
        assertEquals(RiskLevel.INSUFFICIENT_DATA, report.riskLevel());
    }

    @Test
    void resultsAreSortedByScoreAndFilteredByMinimumScore() {
        when(studentRepository.findAll())
                .thenReturn(List.of(student(5, "Good"), student(6, "Medium"), student(4, "Weak")));
        attendanceRows(row(5L, 4L, 4L), row(6L, 3L, 2L), row(4L, 3L, 1L));
        markRows(row(5L, 2L, 85.0, 2L), row(4L, 2L, 30.0, 0L));

        List<RiskReport> all = riskService.allStudents(0);
        List<RiskReport> risky = riskService.allStudents(50);

        assertEquals(List.of(4L, 6L, 5L), all.stream().map(RiskReport::studentId).toList());
        assertEquals(1, risky.size());
        assertEquals(4L, risky.get(0).studentId());
    }

    @Test
    void unknownStudentEmailThrowsNotFound() {
        when(studentRepository.findByUserEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> riskService.forStudent("nobody@test.com"));
    }
}