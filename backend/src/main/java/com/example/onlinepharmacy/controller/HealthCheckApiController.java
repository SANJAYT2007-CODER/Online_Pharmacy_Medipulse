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
@RequestMapping("/api/health-checks")
public class HealthCheckApiController {

    @Autowired
    private HealthPackageRepository packageRepository;

    @Autowired
    private HealthPackageBookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/packages")
    public List<HealthPackage> getPackages() {
        return packageRepository.findAll();
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> bookHealthPackage(@RequestBody Map<String, Object> payload, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized. Please login."));
        }

        String packageName = (String) payload.get("packageName");
        String patientName = (String) payload.getOrDefault("patientName", user.getFullName());
        String bookingDate = (String) payload.get("bookingDate");
        String bookingTime = (String) payload.get("bookingTime");
        BigDecimal price = new BigDecimal(payload.get("price").toString());

        HealthPackageBooking booking = new HealthPackageBooking(user, packageName, patientName, bookingDate, bookingTime, price);
        HealthPackageBooking saved = bookingRepository.save(booking);

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/bookings/my")
    public ResponseEntity<?> getMyPackageBookings(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        User freshUser = userRepository.findById(user.getUserId()).orElse(user);
        return ResponseEntity.ok(bookingRepository.findByUserOrderByCreatedAtDesc(freshUser));
    }
}
