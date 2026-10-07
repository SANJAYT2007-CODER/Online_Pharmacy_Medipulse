package com.example.onlinepharmacy.repository;

import com.example.onlinepharmacy.model.DoctorConsultation;
import com.example.onlinepharmacy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoctorConsultationRepository extends JpaRepository<DoctorConsultation, Long> {
    List<DoctorConsultation> findByUserOrderByCreatedAtDesc(User user);
}
