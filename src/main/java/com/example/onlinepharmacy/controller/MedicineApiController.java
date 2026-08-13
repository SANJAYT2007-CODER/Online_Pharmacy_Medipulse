package com.example.onlinepharmacy.controller;

import com.example.onlinepharmacy.dto.MedicineRequest;
import com.example.onlinepharmacy.model.Category;
import com.example.onlinepharmacy.model.Medicine;
import com.example.onlinepharmacy.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicines")
public class MedicineApiController {

    private final MedicineService medicineService;

    @Autowired
    public MedicineApiController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public ResponseEntity<List<Medicine>> getMedicines(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(medicineService.searchMedicines(search));
        }
        if (categoryId != null && categoryId > 0) {
            return ResponseEntity.ok(medicineService.getMedicinesByCategory(categoryId));
        }
        return ResponseEntity.ok(medicineService.getAllActiveMedicines());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Medicine>> getAllMedicinesForAdmin() {
        return ResponseEntity.ok(medicineService.getAllMedicinesForAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMedicineById(@PathVariable Long id) {
        return medicineService.getMedicineById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMedicine(@RequestBody MedicineRequest request) {
        try {
            Medicine medicine = medicineService.addMedicine(request);
            return ResponseEntity.ok(medicine);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMedicine(@PathVariable Long id, @RequestBody MedicineRequest request) {
        try {
            Medicine medicine = medicineService.updateMedicine(id, request);
            return ResponseEntity.ok(medicine);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedicine(@PathVariable Long id) {
        try {
            medicineService.deleteMedicine(id);
            Map<String, String> res = new HashMap<>();
            res.put("message", "Medicine inactivated successfully.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(medicineService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<?> addCategory(@RequestBody Map<String, String> body) {
        try {
            Category category = medicineService.addCategory(body.get("categoryName"), body.get("description"));
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
