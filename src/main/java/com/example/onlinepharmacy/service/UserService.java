package com.example.onlinepharmacy.service;

import com.example.onlinepharmacy.dto.LoginRequest;
import com.example.onlinepharmacy.dto.RegisterRequest;
import com.example.onlinepharmacy.model.User;
import com.example.onlinepharmacy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email address is already registered.");
        }
        User user = new User(
            request.getFullName(),
            request.getEmail(),
            request.getPassword(), // In production, password would be hashed
            request.getPhone(),
            request.getRole() != null ? request.getRole() : "CUSTOMER",
            "ACTIVE"
        );
        return userRepository.save(user);
    }

    public User loginUser(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(request.getPassword())) {
                return user;
            }
        }
        throw new RuntimeException("Invalid email or password.");
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
