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
@RequestMapping("/api/doctors")
public class DoctorApiController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorConsultationRepository consultationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @GetMapping("/search")
    public List<Doctor> searchDoctors(@RequestParam(required = false) String specialization) {
        if (specialization != null && !specialization.trim().isEmpty()) {
            return doctorRepository.findBySpecializationContainingIgnoreCase(specialization);
        }
        return doctorRepository.findAll();
    }

    @PostMapping("/consultations")
    public ResponseEntity<?> bookConsultation(@RequestBody Map<String, Object> payload, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized. Please login."));
        }

        Long doctorId = Long.valueOf(payload.get("doctorId").toString());
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Doctor not found."));
        }

        String patientName = (String) payload.getOrDefault("patientName", user.getFullName());
        String appointmentDate = (String) payload.get("appointmentDate");
        String appointmentTime = (String) payload.get("appointmentTime");
        String notes = (String) payload.getOrDefault("notes", "");
        BigDecimal fee = doctor.getConsultationFee();

        DoctorConsultation consultation = new DoctorConsultation(
                user, doctor, patientName, appointmentDate, appointmentTime, fee, notes
        );

        DoctorConsultation saved = consultationRepository.save(consultation);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/consultations/my")
    public ResponseEntity<?> getMyConsultations(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized. Please login."));
        }
        User freshUser = userRepository.findById(user.getUserId()).orElse(user);
        return ResponseEntity.ok(consultationRepository.findByUserOrderByCreatedAtDesc(freshUser));
    }
}
