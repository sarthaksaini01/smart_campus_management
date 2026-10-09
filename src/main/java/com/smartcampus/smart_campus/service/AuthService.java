package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.dto.RegisterRequest;
import com.smartcampus.smart_campus.dto.UserResponse;
import com.smartcampus.smart_campus.entity.User;
import com.smartcampus.smart_campus.exception.EmailAlreadyExistsException;
import com.smartcampus.smart_campus.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getRole()
        );
    }
}