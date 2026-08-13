package com.example.onlinepharmacy.controller;

import com.example.onlinepharmacy.model.Prescription;
import com.example.onlinepharmacy.model.User;
import com.example.onlinepharmacy.service.PrescriptionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionApiController {

    private final PrescriptionService prescriptionService;

    @Value("${pharmacy.upload.dir:uploads/}")
    private String uploadDir;

    @Autowired
    public PrescriptionApiController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    private Long getCurrentUserId(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null ? user.getUserId() : 3L; // Fallback to Customer ID
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPrescription(@RequestParam("file") MultipartFile file,
                                                @RequestParam(value = "notes", required = false) String notes,
                                                HttpSession session) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Please select a file to upload.");
            }

            String filename = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
            Path uploadPath = Paths.get("src/main/resources/static/uploads/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path targetPath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/uploads/" + filename;
            Long userId = getCurrentUserId(session);

            Prescription prescription = prescriptionService.uploadPrescription(userId, imageUrl, notes);
            return ResponseEntity.ok(prescription);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<Prescription>> getMyPrescriptions(HttpSession session) {
        Long userId = getCurrentUserId(session);
        return ResponseEntity.ok(prescriptionService.getUserPrescriptions(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Prescription>> getPendingPrescriptions() {
        return ResponseEntity.ok(prescriptionService.getPendingPrescriptions());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Prescription>> getAllPrescriptions() {
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            Prescription prescription = prescriptionService.updatePrescriptionStatus(id, status);
            return ResponseEntity.ok(prescription);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
