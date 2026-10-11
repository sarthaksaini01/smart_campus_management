package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.dto.OverviewStats;
import com.smartcampus.smart_campus.dto.SubjectAttendanceStat;
import com.smartcampus.smart_campus.dto.SubjectPerformance;
import com.smartcampus.smart_campus.entity.AttendanceStatus;
import com.smartcampus.smart_campus.repository.AttendanceRepository;
import com.smartcampus.smart_campus.repository.CourseRepository;
import com.smartcampus.smart_campus.repository.FacultyRepository;
import com.smartcampus.smart_campus.repository.MarkRepository;
import com.smartcampus.smart_campus.repository.NoticeRepository;
import com.smartcampus.smart_campus.repository.StudentRepository;
import com.smartcampus.smart_campus.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final NoticeRepository noticeRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarkRepository markRepository;
    private final RiskService riskService;

    public AnalyticsService(StudentRepository studentRepository,
                            FacultyRepository facultyRepository,
                            CourseRepository courseRepository,
                            SubjectRepository subjectRepository,
                            NoticeRepository noticeRepository,
                            AttendanceRepository attendanceRepository,
                            MarkRepository markRepository,
                            RiskService riskService) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.courseRepository = courseRepository;
        this.subjectRepository = subjectRepository;
        this.noticeRepository = noticeRepository;
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
        this.riskService = riskService;
    }

    @Transactional(readOnly = true)
    public OverviewStats overview() {
        long records = 0;
        long present = 0;
        for (Object[] row : attendanceRepository.totalsBySubject(AttendanceStatus.PRESENT)) {
            records += num(row[3]);
            present += num(row[4]);
        }
        return new OverviewStats(
                studentRepository.count(),
                facultyRepository.count(),
                courseRepository.count(),
                subjectRepository.count(),
                noticeRepository.count(),
                records,
                percent(present, records),
                riskService.allStudents(50).size());
    }

    @Transactional(readOnly = true)
    public List<SubjectAttendanceStat> attendanceBySubject() {
        return attendanceRepository.totalsBySubject(AttendanceStatus.PRESENT).stream()
                .map(r -> new SubjectAttendanceStat(
                        num(r[0]), (String) r[1], (String) r[2],
                        num(r[3]), percent(num(r[4]), num(r[3]))))
                .sorted(Comparator.comparing(SubjectAttendanceStat::subjectCode))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubjectPerformance> subjectPerformance() {
        return markRepository.totalsBySubject(GradeScale.PASS_MARK).stream()
                .map(r -> new SubjectPerformance(
                        num(r[0]), (String) r[1], (String) r[2],
                        num(r[3]),
                        round2(((Number) r[4]).doubleValue()),
                        percent(num(r[5]), num(r[3]))))
                .sorted(Comparator.comparing(SubjectPerformance::subjectCode))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> gradeDistribution() {
        Map<String, Long> result = new LinkedHashMap<>();
        GradeScale.GRADES.forEach(g -> result.put(g, 0L));
        for (Integer total : markRepository.allTotals()) {
            result.merge(GradeScale.grade(total), 1L, Long::sum);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> studentsByCourse() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : studentRepository.countByCourse()) {
            result.put((String) row[0], num(row[1]));
        }
        return result;
    }

    private static long num(Object value) {
        return ((Number) value).longValue();
    }

    private static double percent(long part, long total) {
        return total == 0 ? 0.0 : round2(part * 100.0 / total);
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}