package com.example.onlinepharmacy.service;

import com.example.onlinepharmacy.model.Prescription;
import com.example.onlinepharmacy.model.User;
import com.example.onlinepharmacy.repository.PrescriptionRepository;
import com.example.onlinepharmacy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;

    @Autowired
    public PrescriptionService(PrescriptionRepository prescriptionRepository, UserRepository userRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
    }

    public Prescription uploadPrescription(Long userId, String imageUrl, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Prescription prescription = new Prescription(user, imageUrl, notes, "PENDING");
        return prescriptionRepository.save(prescription);
    }

    public List<Prescription> getUserPrescriptions(Long userId) {
        return prescriptionRepository.findByUser_UserId(userId);
    }

    public List<Prescription> getPendingPrescriptions() {
        return prescriptionRepository.findByStatus("PENDING");
    }

    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    public Prescription updatePrescriptionStatus(Long prescriptionId, String status) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found with ID: " + prescriptionId));
        prescription.setStatus(status);
        return prescriptionRepository.save(prescription);
    }
}
