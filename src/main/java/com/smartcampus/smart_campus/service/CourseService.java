package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.dto.CourseRequest;
import com.smartcampus.smart_campus.dto.CourseResponse;
import com.smartcampus.smart_campus.entity.Course;
import com.smartcampus.smart_campus.exception.DuplicateResourceException;
import com.smartcampus.smart_campus.exception.InvalidRequestException;
import com.smartcampus.smart_campus.exception.ResourceNotFoundException;
import com.smartcampus.smart_campus.repository.CourseRepository;
import com.smartcampus.smart_campus.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;

    public CourseService(CourseRepository courseRepository, SubjectRepository subjectRepository) {
        this.courseRepository = courseRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public CourseResponse create(CourseRequest r) {
        if (courseRepository.existsByCode(r.code())) {
            throw new DuplicateResourceException("Course code already exists");
        }
        if (courseRepository.existsByName(r.name())) {
            throw new DuplicateResourceException("Course name already exists");
        }
        Course c = new Course();
        c.setCode(r.code());
        c.setName(r.name());
        c.setTotalSemesters(r.totalSemesters());
        return toResponse(courseRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAll() {
        return courseRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest r) {
        Course c = findOrThrow(id);
        if (!c.getCode().equals(r.code()) && courseRepository.existsByCode(r.code())) {
            throw new DuplicateResourceException("Course code already exists");
        }
        if (!c.getName().equals(r.name()) && courseRepository.existsByName(r.name())) {
            throw new DuplicateResourceException("Course name already exists");
        }
        c.setCode(r.code());
        c.setName(r.name());
        c.setTotalSemesters(r.totalSemesters());
        return toResponse(courseRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        Course c = findOrThrow(id);
        if (subjectRepository.existsByCourseId(id)) {
            throw new InvalidRequestException("Course has subjects. Delete the subjects first");
        }
        courseRepository.delete(c);
    }

    private Course findOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + id));
    }

    private CourseResponse toResponse(Course c) {
        return new CourseResponse(c.getId(), c.getCode(), c.getName(), c.getTotalSemesters());
    }
}