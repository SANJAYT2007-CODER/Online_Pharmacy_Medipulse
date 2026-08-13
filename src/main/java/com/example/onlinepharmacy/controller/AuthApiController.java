package com.example.onlinepharmacy.controller;

import com.example.onlinepharmacy.dto.LoginRequest;
import com.example.onlinepharmacy.dto.RegisterRequest;
import com.example.onlinepharmacy.model.User;
import com.example.onlinepharmacy.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UserService userService;

    @Autowired
    public AuthApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpSession session) {
        try {
            User user = userService.registerUser(request);
            session.setAttribute("user", user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            User user = userService.loginUser(request);
            session.setAttribute("user", user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not authenticated");
            return ResponseEntity.status(401).body(error);
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/switch-role/{role}")
    public ResponseEntity<?> switchRole(@PathVariable String role, HttpSession session) {
        String email = switch (role.toUpperCase()) {
            case "ADMIN" -> "admin@pharmacy.com";
            case "PHARMACIST" -> "pharmacist@pharmacy.com";
            default -> "customer@pharmacy.com";
        };
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User role account not found"));
        session.setAttribute("user", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
