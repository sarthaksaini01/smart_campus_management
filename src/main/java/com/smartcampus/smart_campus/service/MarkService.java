package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.dto.*;
import com.smartcampus.smart_campus.entity.Mark;
import com.smartcampus.smart_campus.entity.Student;
import com.smartcampus.smart_campus.entity.Subject;
import com.smartcampus.smart_campus.exception.InvalidRequestException;
import com.smartcampus.smart_campus.exception.ResourceNotFoundException;
import com.smartcampus.smart_campus.repository.MarkRepository;
import com.smartcampus.smart_campus.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MarkService {

    private final MarkRepository markRepository;
    private final StudentRepository studentRepository;
    private final SubjectAccessService subjectAccess;

    public MarkService(MarkRepository markRepository,
                       StudentRepository studentRepository,
                       SubjectAccessService subjectAccess) {
        this.markRepository = markRepository;
        this.studentRepository = studentRepository;
        this.subjectAccess = subjectAccess;
    }

    // ---------- Faculty side ----------

    @Transactional
    public int upload(UploadMarksRequest request, String facultyEmail) {
        Subject subject = subjectAccess.ownedSubject(request.subjectId(), facultyEmail);

        Map<Long, Student> eligible = subjectAccess.studentsOf(subject).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        Set<Long> seen = new HashSet<>();

        for (MarkEntry entry : request.records()) {
            Student student = eligible.get(entry.studentId());
            if (student == null) {
                throw new InvalidRequestException(
                        "Student " + entry.studentId() + " is not enrolled in this subject");
            }
            if (!seen.add(entry.studentId())) {
                throw new InvalidRequestException("Duplicate entry for student " + entry.studentId());
            }

            Mark mark = markRepository
                    .findBySubjectIdAndStudentId(subject.getId(), student.getId())
                    .orElseGet(Mark::new);
            mark.setSubject(subject);
            mark.setStudent(student);
            mark.setInternalMarks(entry.internalMarks());
            mark.setExternalMarks(entry.externalMarks());
            markRepository.save(mark);
        }
        return request.records().size();
    }

    @Transactional(readOnly = true)
    public List<SubjectMarkRow> subjectMarks(Long subjectId, String facultyEmail) {
        Subject subject = subjectAccess.ownedSubject(subjectId, facultyEmail);

        Map<Long, Mark> byStudent = markRepository.findBySubjectId(subjectId).stream()
                .collect(Collectors.toMap(m -> m.getStudent().getId(), Function.identity()));

        return subjectAccess.studentsOf(subject).stream().map(st -> {
            Mark m = byStudent.get(st.getId());
            if (m == null) {
                return new SubjectMarkRow(st.getId(), st.getRollNo(), st.getUser().getFullName(),
                        null, null, null, null, null, null);
            }
            int total = total(m);
            return new SubjectMarkRow(st.getId(), st.getRollNo(), st.getUser().getFullName(),
                    m.getInternalMarks(), m.getExternalMarks(), total,
                    GradeScale.grade(total), GradeScale.gradePoint(total),
                    total >= GradeScale.PASS_MARK);
        }).toList();
    }

    // ---------- Student side ----------

    @Transactional(readOnly = true)
    public ResultCard myResults(String studentEmail) {
        Student student = studentRepository.findByUserEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        List<Mark> marks = markRepository.findByStudentId(student.getId());

        Map<Integer, List<Mark>> bySemester = marks.stream()
                .collect(Collectors.groupingBy(m -> m.getSubject().getSemester(),
                        TreeMap::new, Collectors.toList()));

        List<SemesterResult> semesters = new ArrayList<>();
        for (Map.Entry<Integer, List<Mark>> entry : bySemester.entrySet()) {
            List<SubjectResult> subjects = entry.getValue().stream()
                    .sorted(Comparator.comparing((Mark m) -> m.getSubject().getCode()))
                    .map(this::toSubjectResult)
                    .toList();
            semesters.add(new SemesterResult(entry.getKey(), subjects, average(subjects)));
        }

        List<SubjectResult> all = semesters.stream()
                .flatMap(s -> s.subjects().stream())
                .toList();

        return new ResultCard(
                student.getUser().getFullName(),
                student.getRollNo(),
                student.getCourse(),
                semesters,
                average(all));
    }

    // ---------- Helpers ----------

    private SubjectResult toSubjectResult(Mark m) {
        int total = total(m);
        return new SubjectResult(
                m.getSubject().getId(),
                m.getSubject().getCode(),
                m.getSubject().getName(),
                m.getInternalMarks(),
                m.getExternalMarks(),
                total,
                GradeScale.grade(total),
                GradeScale.gradePoint(total),
                total >= GradeScale.PASS_MARK);
    }

    private double average(List<SubjectResult> list) {
        if (list.isEmpty()) {
            return 0.0;
        }
        double avg = list.stream().mapToInt(SubjectResult::gradePoint).average().orElse(0.0);
        return Math.round(avg * 100.0) / 100.0;
    }

    private int total(Mark m) {
        return m.getInternalMarks() + m.getExternalMarks();
    }
}