package com.example.onlinepharmacy.repository;

import com.example.onlinepharmacy.model.DoctorPrescription;
import com.example.onlinepharmacy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoctorPrescriptionRepository extends JpaRepository<DoctorPrescription, Long> {
    List<DoctorPrescription> findByUserOrderByCreatedAtDesc(User user);
}
