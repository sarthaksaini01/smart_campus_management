package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.dto.AuthResponse;
import com.smartcampus.smart_campus.dto.LoginRequest;
import com.smartcampus.smart_campus.dto.RegisterRequest;
import com.smartcampus.smart_campus.dto.UserResponse;
import com.smartcampus.smart_campus.entity.User;
import com.smartcampus.smart_campus.exception.EmailAlreadyExistsException;
import com.smartcampus.smart_campus.exception.InvalidCredentialsException;
import com.smartcampus.smart_campus.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.smartcampus.smart_campus.entity.Role;
import com.smartcampus.smart_campus.exception.InvalidRequestException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new InvalidRequestException("Admin accounts cannot be self-registered");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User saved = userRepository.save(user);

        return new UserResponse(saved.getId(), saved.getFullName(),
                saved.getEmail(), saved.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.isActive()
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, user.getId(), user.getFullName(),
                user.getEmail(), user.getRole());
    }
}