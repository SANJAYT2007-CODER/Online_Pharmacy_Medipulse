package com.example.onlinepharmacy.service;

import com.example.onlinepharmacy.dto.LoginRequest;
import com.example.onlinepharmacy.dto.RegisterRequest;
import com.example.onlinepharmacy.model.PasswordResetToken;
import com.example.onlinepharmacy.model.User;
import com.example.onlinepharmacy.model.VerificationToken;
import com.example.onlinepharmacy.repository.PasswordResetTokenRepository;
import com.example.onlinepharmacy.repository.UserRepository;
import com.example.onlinepharmacy.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository,
                       VerificationTokenRepository verificationTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public User registerUser(RegisterRequest request, String baseUrl) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email address is already registered.");
        }

        User user = new User(
            request.getFullName(),
            request.getEmail(),
            request.getPassword(),
            request.getPhone(),
            request.getRole() != null ? request.getRole() : "CUSTOMER",
            "PENDING_VERIFICATION",
            false
        );

        User savedUser = userRepository.save(user);

        // Generate verification token
        String tokenStr = UUID.randomUUID().toString();
        VerificationToken token = new VerificationToken(tokenStr, savedUser, LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(token);

        // Construct link and send email
        String verificationUrl = baseUrl + "/verify-email?token=" + tokenStr;
        try {
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFullName(), verificationUrl);
        } catch (Exception e) {
            logger.warn("Could not dispatch verification email for user {}: {}", savedUser.getEmail(), e.getMessage());
        }

        return savedUser;
    }

    @Transactional
    public User verifyEmail(String token) {
        VerificationToken vToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token."));

        if (vToken.isUsed()) {
            throw new RuntimeException("This verification link has already been used.");
        }

        if (vToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification link has expired. Please register again.");
        }

        vToken.setUsed(true);
        verificationTokenRepository.save(vToken);

        User user = vToken.getUser();
        user.setEmailVerified(true);
        user.setStatus("ACTIVE");
        return userRepository.save(user);
    }

    public User loginUser(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(request.getPassword())) {
                if (!user.isEmailVerified() || "PENDING_VERIFICATION".equalsIgnoreCase(user.getStatus())) {
                    throw new RuntimeException("Account is not verified. Please check your email inbox to verify your account before logging in.");
                }
                
                // Send Login Notification Email
                try {
                    emailService.sendLoginNotification(user.getEmail(), user.getFullName(), LocalDateTime.now());
                } catch (Exception e) {
                    logger.warn("Could not dispatch login notification email to {}: {}", user.getEmail(), e.getMessage());
                }

                return user;
            }
        }
        throw new RuntimeException("Invalid email or password.");
    }

    @Transactional
    public void processForgotPassword(String email, String baseUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No registered user found with email: " + email));

        String tokenStr = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(tokenStr, user, LocalDateTime.now().plusHours(1));
        passwordResetTokenRepository.save(resetToken);

        String resetUrl = baseUrl + "/reset-password?token=" + tokenStr;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetUrl);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken rToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token."));

        if (rToken.isUsed()) {
            throw new RuntimeException("This password reset token has already been used.");
        }

        if (rToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Password reset link has expired. Please request a new one.");
        }

        User user = rToken.getUser();
        user.setPassword(newPassword);
        userRepository.save(user);

        rToken.setUsed(true);
        passwordResetTokenRepository.save(rToken);
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
