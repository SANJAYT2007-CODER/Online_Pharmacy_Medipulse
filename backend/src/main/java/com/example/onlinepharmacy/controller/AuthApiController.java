package com.example.onlinepharmacy.controller;

import com.example.onlinepharmacy.dto.LoginRequest;
import com.example.onlinepharmacy.dto.RegisterRequest;
import com.example.onlinepharmacy.model.User;
import com.example.onlinepharmacy.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        try {
            String baseUrl = getBaseUrl(servletRequest);
            User user = userService.registerUser(request, baseUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful! A verification email has been sent to " + user.getEmail() + ". Please verify your email before logging in.");
            response.put("email", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmailApi(@RequestParam("token") String token) {
        try {
            User user = userService.verifyEmail(token);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Email verified successfully! You can now log in.");
            response.put("user", user);
            return ResponseEntity.ok(response);
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body, HttpServletRequest servletRequest) {
        try {
            String email = body.get("email");
            if (email == null || email.isBlank()) {
                throw new RuntimeException("Email address is required.");
            }
            String baseUrl = getBaseUrl(servletRequest);
            userService.processForgotPassword(email, baseUrl);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset instructions have been sent to your email address.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            String token = body.get("token");
            String newPassword = body.get("newPassword");
            if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
                throw new RuntimeException("Token and new password are required.");
            }

            userService.resetPassword(token, newPassword);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully! You can now log in with your new password.");
            return ResponseEntity.ok(response);
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

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        if (("http".equals(scheme) && serverPort != 80) || ("https".equals(scheme) && serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        url.append(contextPath);
        return url.toString();
    }
}
