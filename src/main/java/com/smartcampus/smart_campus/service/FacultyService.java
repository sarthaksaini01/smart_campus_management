package com.smartcampus.smart_campus.service;
import com.smartcampus.smart_campus.dto.FacultyResponse;
import com.smartcampus.smart_campus.dto.CreateFacultyRequest;
import com.smartcampus.smart_campus.dto.UpdateFacultyRequest;
import com.smartcampus.smart_campus.entity.Faculty;
import com.smartcampus.smart_campus.entity.Role;
import com.smartcampus.smart_campus.entity.User;
import com.smartcampus.smart_campus.exception.DuplicateResourceException;
import com.smartcampus.smart_campus.exception.EmailAlreadyExistsException;
import com.smartcampus.smart_campus.exception.ResourceNotFoundException;
import com.smartcampus.smart_campus.repository.FacultyRepository;
import com.smartcampus.smart_campus.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public FacultyService(FacultyRepository facultyRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.facultyRepository = facultyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public FacultyResponse create(CreateFacultyRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
        if (facultyRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Employee ID already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.FACULTY);
        user = userRepository.save(user);

        Faculty faculty = new Faculty();
        faculty.setUser(user);
        faculty.setEmployeeId(request.getEmployeeId());
        faculty.setDepartment(request.getDepartment());

        return toResponse(facultyRepository.save(faculty));
    }

    @Transactional(readOnly = true)
    public List<FacultyResponse> getAll() {
        return facultyRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FacultyResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public FacultyResponse getByEmail(String email) {
        Faculty faculty = facultyRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found"));
        return toResponse(faculty);
    }

    @Transactional
    public FacultyResponse update(Long id, UpdateFacultyRequest request) {
        Faculty faculty = findOrThrow(id);
        faculty.getUser().setFullName(request.getFullName());
        faculty.setDepartment(request.getDepartment());
        return toResponse(facultyRepository.save(faculty));
    }

    @Transactional
    public void delete(Long id) {
        Faculty faculty = findOrThrow(id);
        User user = faculty.getUser();
        facultyRepository.delete(faculty);
        userRepository.delete(user);
    }

    private Faculty findOrThrow(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id " + id));
    }

    private FacultyResponse toResponse(Faculty f) {
        return new FacultyResponse(
                f.getId(),
                f.getUser().getId(),
                f.getUser().getFullName(),
                f.getUser().getEmail(),
                f.getEmployeeId(),
                f.getDepartment());
    }
}