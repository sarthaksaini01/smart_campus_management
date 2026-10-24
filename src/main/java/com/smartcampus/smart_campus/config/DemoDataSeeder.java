package com.smartcampus.smart_campus.config;

import com.smartcampus.smart_campus.entity.Attendance;
import com.smartcampus.smart_campus.entity.AttendanceStatus;
import com.smartcampus.smart_campus.entity.Course;
import com.smartcampus.smart_campus.entity.Faculty;
import com.smartcampus.smart_campus.entity.Mark;
import com.smartcampus.smart_campus.entity.Notice;
import com.smartcampus.smart_campus.entity.NoticeAudience;
import com.smartcampus.smart_campus.entity.Role;
import com.smartcampus.smart_campus.entity.Student;
import com.smartcampus.smart_campus.entity.Subject;
import com.smartcampus.smart_campus.entity.User;
import com.smartcampus.smart_campus.repository.AttendanceRepository;
import com.smartcampus.smart_campus.repository.CourseRepository;
import com.smartcampus.smart_campus.repository.FacultyRepository;
import com.smartcampus.smart_campus.repository.MarkRepository;
import com.smartcampus.smart_campus.repository.NoticeRepository;
import com.smartcampus.smart_campus.repository.StudentRepository;
import com.smartcampus.smart_campus.repository.SubjectRepository;
import com.smartcampus.smart_campus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Creates sample data for demos. Runs only when app.demo.seed=true and the database has no courses.
 * With app.demo.reset=true it first deletes all demo data (users with the @demo.edu domain and
 * every course, subject, student, faculty, attendance, mark and notice).
 */
@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private static final String DEMO_PASSWORD = "Demo@1234";
    private static final String DOMAIN = "@demo.edu";
    private static final int CLASS_DAYS = 10;

    private enum Profile {
        GOOD(0.94, 80),
        AVERAGE(0.82, 62),
        WEAK(0.68, 47),
        CRITICAL(0.45, 32);

        private final double attendance;
        private final int marks;

        Profile(double attendance, int marks) {
            this.attendance = attendance;
            this.marks = marks;
        }
    }

    private record Seed(String name, Profile profile) {}

    private record Enrolled(Student student, Profile profile) {}

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarkRepository markRepository;
    private final NoticeRepository noticeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbc;
    private final boolean seed;
    private final boolean reset;

    public DemoDataSeeder(UserRepository userRepository,
                          CourseRepository courseRepository,
                          FacultyRepository facultyRepository,
                          StudentRepository studentRepository,
                          SubjectRepository subjectRepository,
                          AttendanceRepository attendanceRepository,
                          MarkRepository markRepository,
                          NoticeRepository noticeRepository,
                          PasswordEncoder passwordEncoder,
                          JdbcTemplate jdbc,
                          @Value("${app.demo.seed:false}") boolean seed,
                          @Value("${app.demo.reset:false}") boolean reset) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
        this.noticeRepository = noticeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbc = jdbc;
        this.seed = seed;
        this.reset = reset;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (reset) {
            wipe();
        }
        if (!seed) {
            return;
        }
        if (courseRepository.count() > 0) {
            log.info("Demo data skipped: the database already has courses");
            return;
        }
        seedAll();
    }

    private void wipe() {
        log.warn("Demo reset: deleting all demo data");
        for (String table : List.of("attendance", "marks", "notices", "subjects", "students", "faculty", "courses")) {
            jdbc.update("DELETE FROM " + table);
        }
        jdbc.update("DELETE FROM users WHERE email LIKE ?", "%" + DOMAIN);
    }

    private void seedAll() {
        String hash = passwordEncoder.encode(DEMO_PASSWORD);
        Random random = new Random(42);

        User admin = user("Demo Admin", "admin" + DOMAIN, Role.ADMIN, hash);

        Course cse = course("BTCSE", "B.Tech CSE");
        Course ece = course("BTECE", "B.Tech ECE");

        Faculty anita = faculty("Dr. Anita Verma", "anita" + DOMAIN, "EMP1001", "Computer Science", hash);
        Faculty rohan = faculty("Prof. Rohan Mehta", "rohan" + DOMAIN, "EMP1002", "Computer Science", hash);
        Faculty kavita = faculty("Dr. Kavita Rao", "kavita" + DOMAIN, "EMP1003", "Electronics", hash);

        List<Subject> subjects = List.of(
                subject("CS501", "Database Management Systems", 5, cse, anita),
                subject("CS502", "Operating Systems", 5, cse, rohan),
                subject("CS503", "Computer Networks", 5, cse, rohan),
                subject("CS504", "Software Engineering", 5, cse, anita),
                subject("EC301", "Signals and Systems", 3, ece, kavita),
                subject("EC302", "Digital Electronics", 3, ece, kavita));

        List<Enrolled> enrolled = new ArrayList<>();
        addStudents(enrolled, cse, 5, "CSE2023", hash, List.of(
                new Seed("Aarav Sharma", Profile.GOOD),
                new Seed("Diya Patel", Profile.GOOD),
                new Seed("Rohit Kumar", Profile.GOOD),
                new Seed("Sneha Iyer", Profile.GOOD),
                new Seed("Karan Malhotra", Profile.GOOD),
                new Seed("Priya Singh", Profile.AVERAGE),
                new Seed("Vikram Joshi", Profile.AVERAGE),
                new Seed("Neha Gupta", Profile.AVERAGE),
                new Seed("Arjun Nair", Profile.AVERAGE),
                new Seed("Pooja Reddy", Profile.AVERAGE),
                new Seed("Rahul Verma", Profile.WEAK),
                new Seed("Simran Kaur", Profile.WEAK),
                new Seed("Manish Yadav", Profile.CRITICAL),
                new Seed("Kunal Bansal", Profile.CRITICAL)));
        addStudents(enrolled, ece, 3, "ECE2023", hash, List.of(
                new Seed("Ishita Das", Profile.GOOD),
                new Seed("Harsh Vardhan", Profile.GOOD),
                new Seed("Meera Pillai", Profile.AVERAGE),
                new Seed("Sahil Khan", Profile.AVERAGE),
                new Seed("Tanvi Desai", Profile.WEAK),
                new Seed("Nikhil Chauhan", Profile.CRITICAL)));

        List<LocalDate> dates = classDates();
        int attendanceRows = 0;
        for (Subject subject : subjects) {
            for (Enrolled e : enrolled) {
                Student student = e.student();
                boolean inSubject = student.getCourse().equals(subject.getCourse().getName())
                        && student.getSemester().equals(subject.getSemester());
                if (!inSubject) {
                    continue;
                }
                for (LocalDate date : dates) {
                    Attendance a = new Attendance();
                    a.setSubject(subject);
                    a.setStudent(student);
                    a.setAttendanceDate(date);
                    a.setStatus(random.nextDouble() < e.profile().attendance
                            ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT);
                    attendanceRepository.save(a);
                    attendanceRows++;
                }
                markRepository.save(mark(subject, student, e.profile(), random));
            }
        }

        notice(admin, "Mid-semester exams from 20 November",
                "Mid-semester exams start on 20 November. Check the schedule with your department.",
                NoticeAudience.ALL);
        notice(admin, "Minimum 75% attendance is compulsory",
                "Students below 75% attendance in any subject may not be allowed to sit the exams.",
                NoticeAudience.STUDENT);
        notice(admin, "Faculty meeting on Monday",
                "Department heads and faculty are requested to attend the staff meeting at 10 AM.",
                NoticeAudience.FACULTY);
        notice(anita.getUser(), "DBMS assignment due Friday",
                "Submit the ER diagram and normalisation assignment by Friday.",
                NoticeAudience.STUDENT);

        log.info("Demo data created: {} students, 3 faculty, {} subjects, {} attendance records",
                enrolled.size(), subjects.size(), attendanceRows);
    }

    private void addStudents(List<Enrolled> target, Course course, int semester, String rollPrefix,
                             String hash, List<Seed> seeds) {
        for (int i = 0; i < seeds.size(); i++) {
            Seed s = seeds.get(i);
            String email = s.name().toLowerCase().replace(' ', '.') + DOMAIN;
            User user = user(s.name(), email, Role.STUDENT, hash);

            Student student = new Student();
            student.setUser(user);
            student.setRollNo(rollPrefix + String.format("%03d", i + 1));
            student.setCourse(course.getName());
            student.setSemester(semester);
            target.add(new Enrolled(studentRepository.save(student), s.profile()));
        }
    }

    private List<LocalDate> classDates() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate day = LocalDate.now().minusDays(1);
        while (dates.size() < CLASS_DAYS) {
            if (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY) {
                dates.add(day);
            }
            day = day.minusDays(1);
        }
        Collections.reverse(dates);
        return dates;
    }

    private Mark mark(Subject subject, Student student, Profile profile, Random random) {
        int total = clamp(profile.marks + random.nextInt(21) - 10, 5, 98);
        int internal = clamp((int) Math.round(total * 0.4) + random.nextInt(5) - 2, 0, 40);
        int external = clamp(total - internal, 0, 60);

        Mark mark = new Mark();
        mark.setSubject(subject);
        mark.setStudent(student);
        mark.setInternalMarks(internal);
        mark.setExternalMarks(external);
        return mark;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private User user(String fullName, String email, Role role, String hash) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(hash);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Course course(String code, String name) {
        Course course = new Course();
        course.setCode(code);
        course.setName(name);
        course.setTotalSemesters(8);
        return courseRepository.save(course);
    }

    private Faculty faculty(String name, String email, String employeeId, String department, String hash) {
        Faculty faculty = new Faculty();
        faculty.setUser(user(name, email, Role.FACULTY, hash));
        faculty.setEmployeeId(employeeId);
        faculty.setDepartment(department);
        return facultyRepository.save(faculty);
    }

    private Subject subject(String code, String name, int semester, Course course, Faculty faculty) {
        Subject subject = new Subject();
        subject.setCode(code);
        subject.setName(name);
        subject.setSemester(semester);
        subject.setCourse(course);
        subject.setFaculty(faculty);
        return subjectRepository.save(subject);
    }

    private void notice(User author, String title, String content, NoticeAudience audience) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setAudience(audience);
        notice.setCreatedBy(author);
        noticeRepository.save(notice);
    }
}