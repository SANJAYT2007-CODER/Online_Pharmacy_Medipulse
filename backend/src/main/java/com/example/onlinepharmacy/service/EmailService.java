package com.example.onlinepharmacy.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends email verification link after successful user registration.
     */
    public void sendVerificationEmail(String toEmail, String fullName, String verificationUrl) {
        String subject = "Verify Your Email Address - MediPulse Online Pharmacy";
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; }
                    .card { max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); border: 1px solid #e2e8f0; }
                    .header { font-size: 24px; font-weight: 700; color: #0284c7; margin-bottom: 16px; }
                    .content { font-size: 15px; color: #334155; line-height: 1.6; margin-bottom: 24px; }
                    .btn { display: inline-block; background-color: #0284c7; color: #ffffff !important; padding: 12px 28px; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 15px; margin: 16px 0; }
                    .footer { font-size: 12px; color: #94a3b8; border-top: 1px solid #f1f5f9; padding-top: 16px; margin-top: 24px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">MediPulse Online Pharmacy</div>
                    <div class="content">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>Thank you for creating an account with MediPulse. Please confirm your email address by clicking the verification button below:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="btn">Verify Email Address</a>
                        </p>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #0284c7;"><a href="%s">%s</a></p>
                        <p>This link will expire in 24 hours. If you did not create this account, no action is required.</p>
                    </div>
                    <div class="footer">
                        &copy; %d MediPulse Online Pharmacy Platform. All rights reserved.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fullName, verificationUrl, verificationUrl, verificationUrl, LocalDateTime.now().getYear());

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    /**
     * Sends login notification alert after successful authentication.
     */
    public void sendLoginNotification(String toEmail, String fullName, LocalDateTime loginTime) {
        String subject = "New Login Notification - Online Pharmacy";
        String formattedTime = loginTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' hh:mm:ss a (z)"));

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; }
                    .card { max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); border: 1px solid #e2e8f0; }
                    .header { font-size: 24px; font-weight: 700; color: #0284c7; margin-bottom: 16px; }
                    .content { font-size: 15px; color: #334155; line-height: 1.6; margin-bottom: 24px; }
                    .info-box { background-color: #f1f5f9; border-left: 4px solid #0284c7; padding: 14px 18px; border-radius: 4px; margin: 16px 0; }
                    .security-notice { background-color: #fef2f2; border-left: 4px solid #ef4444; color: #991b1b; padding: 14px 18px; border-radius: 4px; margin: 16px 0; font-size: 14px; }
                    .footer { font-size: 12px; color: #94a3b8; border-top: 1px solid #f1f5f9; padding-top: 16px; margin-top: 24px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">MediPulse Online Pharmacy</div>
                    <div class="content">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>We detected a new successful login to your MediPulse account.</p>
                        <div class="info-box">
                            <strong>Login Timestamp:</strong> %s<br>
                            <strong>Account Email:</strong> %s
                        </div>
                        <div class="security-notice">
                            <strong>Security Message:</strong> If you performed this login, you can safely ignore this email. If you did not log in, please reset your password immediately or contact our support team to secure your account.
                        </div>
                    </div>
                    <div class="footer">
                        &copy; %d MediPulse Online Pharmacy Platform. All rights reserved.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fullName, formattedTime, toEmail, LocalDateTime.now().getYear());

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    /**
     * Sends password reset link email.
     */
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetUrl) {
        String subject = "Password Reset Request - MediPulse Online Pharmacy";
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; }
                    .card { max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); border: 1px solid #e2e8f0; }
                    .header { font-size: 24px; font-weight: 700; color: #0284c7; margin-bottom: 16px; }
                    .content { font-size: 15px; color: #334155; line-height: 1.6; margin-bottom: 24px; }
                    .btn { display: inline-block; background-color: #ef4444; color: #ffffff !important; padding: 12px 28px; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 15px; margin: 16px 0; }
                    .footer { font-size: 12px; color: #94a3b8; border-top: 1px solid #f1f5f9; padding-top: 16px; margin-top: 24px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">MediPulse Online Pharmacy</div>
                    <div class="content">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>We received a request to reset the password for your account associated with <strong>%s</strong>.</p>
                        <p style="text-align: center;">
                            <a href="%s" class="btn">Reset Password</a>
                        </p>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #ef4444;"><a href="%s">%s</a></p>
                        <p>This password reset token is valid for 1 hour and can only be used once.</p>
                        <p>If you did not request a password reset, please ignore this message; your password will remain unchanged.</p>
                    </div>
                    <div class="footer">
                        &copy; %d MediPulse Online Pharmacy Platform. All rights reserved.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fullName, toEmail, resetUrl, resetUrl, resetUrl, LocalDateTime.now().getYear());

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            } else {
                helper.setFrom("noreply@onlinepharmacy.com");
            }
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Successfully sent email to {} with subject: {}", toEmail, subject);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Email dispatch failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Email dispatch failed: " + e.getMessage(), e);
        }
    }
}
