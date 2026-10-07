package com.example.onlinepharmacy.controller;

import com.example.onlinepharmacy.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebViewController {

    private final UserService userService;

    @Autowired
    public WebViewController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/", "/medicines", "/doctor-consult", "/lab-tests", "/healthcare-products", "/health-check", "/health-blogs", "/branded-substitute", "/plus", "/offers", "/value-store"})
    public String index(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "index";
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam(name = "token", required = false) String token, Model model) {
        model.addAttribute("token", token);
        if (token != null && !token.isBlank()) {
            try {
                userService.verifyEmail(token);
                model.addAttribute("success", true);
                model.addAttribute("message", "Your email address has been verified successfully! You can now log in to your account.");
            } catch (Exception e) {
                model.addAttribute("success", false);
                model.addAttribute("message", e.getMessage());
            }
        } else {
            model.addAttribute("success", false);
            model.addAttribute("message", "No verification token provided.");
        }
        return "verify-email";
    }

    @GetMapping("/reset-password")
    public String resetPasswordView(@RequestParam(name = "token", required = false) String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }
}
