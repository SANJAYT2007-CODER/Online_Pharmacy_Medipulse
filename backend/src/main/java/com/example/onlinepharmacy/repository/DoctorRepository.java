package com.example.onlinepharmacy.repository;

import com.example.onlinepharmacy.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByStatus(String status);
    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);
}
