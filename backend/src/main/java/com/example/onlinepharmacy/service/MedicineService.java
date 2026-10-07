package com.example.onlinepharmacy.service;

import com.example.onlinepharmacy.dto.MedicineRequest;
import com.example.onlinepharmacy.model.Category;
import com.example.onlinepharmacy.model.Medicine;
import com.example.onlinepharmacy.repository.CategoryRepository;
import com.example.onlinepharmacy.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public MedicineService(MedicineRepository medicineRepository, CategoryRepository categoryRepository) {
        this.medicineRepository = medicineRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Medicine> getAllActiveMedicines() {
        return medicineRepository.findByStatus("ACTIVE");
    }

    public List<Medicine> getAllMedicinesForAdmin() {
        return medicineRepository.findAll();
    }

    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }

    public List<Medicine> getMedicinesByCategory(Long categoryId) {
        return medicineRepository.findByCategory_CategoryIdAndStatus(categoryId, "ACTIVE");
    }

    public List<Medicine> searchMedicines(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllActiveMedicines();
        }
        return medicineRepository.searchMedicines(query.trim());
    }

    public Medicine addMedicine(MedicineRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));

        Medicine medicine = new Medicine(
            category,
            request.getMedicineName(),
            request.getDescription(),
            request.getBrand(),
            request.getPrice(),
            request.getDiscount(),
            request.getStockQuantity(),
            request.getRequiresPrescription(),
            request.getImageUrl(),
            "ACTIVE"
        );
        return medicineRepository.save(medicine);
    }

    public Medicine updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with ID: " + id));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));
            medicine.setCategory(category);
        }

        if (request.getMedicineName() != null) medicine.setMedicineName(request.getMedicineName());
        if (request.getDescription() != null) medicine.setDescription(request.getDescription());
        if (request.getBrand() != null) medicine.setBrand(request.getBrand());
        if (request.getPrice() != null) medicine.setPrice(request.getPrice());
        if (request.getDiscount() != null) medicine.setDiscount(request.getDiscount());
        if (request.getStockQuantity() != null) medicine.setStockQuantity(request.getStockQuantity());
        if (request.getRequiresPrescription() != null) medicine.setRequiresPrescription(request.getRequiresPrescription());
        if (request.getImageUrl() != null) medicine.setImageUrl(request.getImageUrl());

        return medicineRepository.save(medicine);
    }

    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with ID: " + id));
        medicine.setStatus("INACTIVE");
        medicineRepository.save(medicine);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category addCategory(String name, String description) {
        Category category = new Category(name, description, "ACTIVE");
        return categoryRepository.save(category);
    }
}
