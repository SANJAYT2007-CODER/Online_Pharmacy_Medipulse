package com.example.onlinepharmacy.controller;

import com.example.onlinepharmacy.model.*;
import com.example.onlinepharmacy.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor-prescriptions")
public class DoctorPrescriptionApiController {

    @Autowired
    private DoctorPrescriptionRepository prescriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @GetMapping("/my")
    public ResponseEntity<?> getMyPrescriptions(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        User freshUser = userRepository.findById(user.getUserId()).orElse(user);
        return ResponseEntity.ok(prescriptionRepository.findByUserOrderByCreatedAtDesc(freshUser));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPrescriptions() {
        return ResponseEntity.ok(prescriptionRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createPrescription(@RequestBody Map<String, Object> payload, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        Long patientUserId = payload.containsKey("patientUserId") && payload.get("patientUserId") != null
                ? Long.valueOf(payload.get("patientUserId").toString())
                : currentUser.getUserId();

        User patientUser = userRepository.findById(patientUserId).orElse(currentUser);

        String doctorName = (String) payload.getOrDefault("doctorName", "Dr. Sarah Smith, MD");
        String specialization = (String) payload.getOrDefault("doctorSpecialization", "General Physician");
        String patientName = (String) payload.getOrDefault("patientName", patientUser.getFullName());
        String date = (String) payload.getOrDefault("consultationDate", "Today");
        String notes = (String) payload.getOrDefault("diagnosisNotes", "Take prescribed medication as directed.");
        String status = (String) payload.getOrDefault("status", "Verified");

        DoctorPrescription rx = new DoctorPrescription(patientUser, doctorName, specialization, patientName, date, notes, status);

        if (payload.containsKey("medicines") && payload.get("medicines") instanceof List) {
            List<Map<String, Object>> medList = (List<Map<String, Object>>) payload.get("medicines");
            List<PrescriptionItem> items = new ArrayList<>();

            for (Map<String, Object> m : medList) {
                String medName = (String) m.get("medicineName");
                String dosage = (String) m.getOrDefault("dosage", "1 tablet");
                String frequency = (String) m.getOrDefault("frequency", "Twice daily");
                String duration = (String) m.getOrDefault("duration", "5 days");
                String instructions = (String) m.getOrDefault("instructions", "Take after food");
                String timing = (String) m.getOrDefault("timing", "After food");
                BigDecimal price = m.containsKey("price") ? new BigDecimal(m.get("price").toString()) : BigDecimal.valueOf(150);

                Long medId = null;
                if (m.containsKey("medicineId") && m.get("medicineId") != null) {
                    medId = Long.valueOf(m.get("medicineId").toString());
                }

                PrescriptionItem item = new PrescriptionItem(medName, dosage, frequency, duration, instructions, timing, price, medId);
                items.add(item);
            }
            rx.setItems(items);
        }

        DoctorPrescription saved = prescriptionRepository.save(rx);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{rxId}/status")
    public ResponseEntity<?> updateRxStatus(@PathVariable Long rxId, @RequestBody Map<String, String> body) {
        DoctorPrescription rx = prescriptionRepository.findById(rxId).orElse(null);
        if (rx == null) {
            return ResponseEntity.notFound().build();
        }
        String newStatus = body.get("status");
        if (newStatus != null) {
            rx.setStatus(newStatus);
            prescriptionRepository.save(rx);
        }
        return ResponseEntity.ok(rx);
    }
}
