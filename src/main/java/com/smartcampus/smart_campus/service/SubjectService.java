package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.dto.SubjectRequest;
import com.smartcampus.smart_campus.dto.SubjectResponse;
import com.smartcampus.smart_campus.entity.Course;
import com.smartcampus.smart_campus.entity.Faculty;
import com.smartcampus.smart_campus.entity.Subject;
import com.smartcampus.smart_campus.exception.DuplicateResourceException;
import com.smartcampus.smart_campus.exception.InvalidRequestException;
import com.smartcampus.smart_campus.exception.ResourceNotFoundException;
import com.smartcampus.smart_campus.repository.CourseRepository;
import com.smartcampus.smart_campus.repository.FacultyRepository;
import com.smartcampus.smart_campus.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;

    public SubjectService(SubjectRepository subjectRepository,
                          CourseRepository courseRepository,
                          FacultyRepository facultyRepository) {
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
        this.facultyRepository = facultyRepository;
    }

    @Transactional
    public SubjectResponse create(SubjectRequest r) {
        if (subjectRepository.existsByCode(r.code())) {
            throw new DuplicateResourceException("Subject code already exists");
        }
        Course course = findCourse(r.courseId());
        checkSemester(r.semester(), course);

        Subject s = new Subject();
        s.setCode(r.code());
        s.setName(r.name());
        s.setSemester(r.semester());
        s.setCourse(course);
        return toResponse(subjectRepository.save(s));
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAll(Long courseId) {
        List<Subject> list = (courseId == null)
                ? subjectRepository.findAll()
                : subjectRepository.findByCourseId(courseId);
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getByFacultyEmail(String email) {
        return subjectRepository.findByFacultyUserEmail(email).stream().map(this::toResponse).toList();
    }

    @Transactional
    public SubjectResponse update(Long id, SubjectRequest r) {
        Subject s = findOrThrow(id);
        if (!s.getCode().equals(r.code()) && subjectRepository.existsByCode(r.code())) {
            throw new DuplicateResourceException("Subject code already exists");
        }
        Course course = findCourse(r.courseId());
        checkSemester(r.semester(), course);

        s.setCode(r.code());
        s.setName(r.name());
        s.setSemester(r.semester());
        s.setCourse(course);
        return toResponse(subjectRepository.save(s));
    }

    @Transactional
    public SubjectResponse assignFaculty(Long subjectId, Long facultyId) {
        Subject s = findOrThrow(subjectId);
        Faculty f = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id " + facultyId));
        s.setFaculty(f);
        return toResponse(subjectRepository.save(s));
    }

    @Transactional
    public void delete(Long id) {
        subjectRepository.delete(findOrThrow(id));
    }

    private Subject findOrThrow(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + id));
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + courseId));
    }

    private void checkSemester(Integer semester, Course course) {
        if (semester > course.getTotalSemesters()) {
            throw new InvalidRequestException(
                    "Semester cannot be more than " + course.getTotalSemesters() + " for this course");
        }
    }

    private SubjectResponse toResponse(Subject s) {
        Faculty f = s.getFaculty();
        return new SubjectResponse(
                s.getId(),
                s.getCode(),
                s.getName(),
                s.getSemester(),
                s.getCourse().getId(),
                s.getCourse().getName(),
                f == null ? null : f.getId(),
                f == null ? null : f.getUser().getFullName());
    }
}