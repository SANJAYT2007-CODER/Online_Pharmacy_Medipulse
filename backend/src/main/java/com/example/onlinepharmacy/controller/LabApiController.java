package com.example.onlinepharmacy.controller;

import com.example.onlinepharmacy.model.*;
import com.example.onlinepharmacy.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/labs")
public class LabApiController {

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private LabBookingRepository labBookingRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/tests")
    public List<LabTest> getLabTests(@RequestParam(required = false) String category) {
        if (category != null && !category.trim().isEmpty()) {
            return labTestRepository.findByCategory(category);
        }
        return labTestRepository.findAll();
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> bookLabTest(@RequestBody Map<String, Object> payload, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized. Please login."));
        }

        String testName = (String) payload.get("testName");
        String patientName = (String) payload.getOrDefault("patientName", user.getFullName());
        String bookingDate = (String) payload.get("bookingDate");
        String bookingTime = (String) payload.get("bookingTime");
        BigDecimal totalPrice = new BigDecimal(payload.get("price").toString());

        LabBooking booking = new LabBooking(user, testName, patientName, bookingDate, bookingTime, totalPrice);
        LabBooking saved = labBookingRepository.save(booking);

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/bookings/my")
    public ResponseEntity<?> getMyBookings(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        User freshUser = userRepository.findById(user.getUserId()).orElse(user);
        return ResponseEntity.ok(labBookingRepository.findByUserOrderByCreatedAtDesc(freshUser));
    }
}
