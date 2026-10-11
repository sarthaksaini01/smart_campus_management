package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.entity.Student;
import com.smartcampus.smart_campus.entity.Subject;
import com.smartcampus.smart_campus.exception.ForbiddenException;
import com.smartcampus.smart_campus.exception.ResourceNotFoundException;
import com.smartcampus.smart_campus.repository.StudentRepository;
import com.smartcampus.smart_campus.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectAccessService {

    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;

    public SubjectAccessService(SubjectRepository subjectRepository,
                                StudentRepository studentRepository) {
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
    }

    public Subject ownedSubject(Long subjectId, String facultyEmail) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + subjectId));
        if (subject.getFaculty() == null
                || !subject.getFaculty().getUser().getEmail().equals(facultyEmail)) {
            throw new ForbiddenException("You are not assigned to this subject");
        }
        return subject;
    }

    public List<Student> studentsOf(Subject subject) {
        return studentRepository.findByCourseAndSemester(
                subject.getCourse().getName(), subject.getSemester());
    }
}