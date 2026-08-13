package com.example.onlinepharmacy.repository;

import com.example.onlinepharmacy.model.Prescription;
import com.example.onlinepharmacy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByUser(User user);
    List<Prescription> findByUser_UserId(Long userId);
    List<Prescription> findByStatus(String status);
}
