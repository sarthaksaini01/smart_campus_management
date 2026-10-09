package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.dto.CreateStudentRequest;
import com.smartcampus.smart_campus.dto.StudentResponse;
import com.smartcampus.smart_campus.dto.UpdateStudentRequest;
import com.smartcampus.smart_campus.entity.Role;
import com.smartcampus.smart_campus.entity.Student;
import com.smartcampus.smart_campus.entity.User;
import com.smartcampus.smart_campus.exception.DuplicateResourceException;
import com.smartcampus.smart_campus.exception.EmailAlreadyExistsException;
import com.smartcampus.smart_campus.exception.ResourceNotFoundException;
import com.smartcampus.smart_campus.repository.StudentRepository;
import com.smartcampus.smart_campus.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
        if (studentRepository.existsByRollNo(request.getRollNo())) {
            throw new DuplicateResourceException("Roll number already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        user = userRepository.save(user);

        Student student = new Student();
        student.setUser(user);
        student.setRollNo(request.getRollNo());
        student.setCourse(request.getCourse());
        student.setSemester(request.getSemester());

        return toResponse(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAll() {
        return studentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public StudentResponse getByEmail(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        return toResponse(student);
    }

    @Transactional
    public StudentResponse update(Long id, UpdateStudentRequest request) {
        Student student = findOrThrow(id);
        student.getUser().setFullName(request.getFullName());
        student.setCourse(request.getCourse());
        student.setSemester(request.getSemester());
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public void delete(Long id) {
        Student student = findOrThrow(id);
        User user = student.getUser();
        studentRepository.delete(student);
        userRepository.delete(user);
    }

    private Student findOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
    }

    private StudentResponse toResponse(Student s) {
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