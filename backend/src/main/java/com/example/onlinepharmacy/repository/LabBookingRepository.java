package com.example.onlinepharmacy.repository;

import com.example.onlinepharmacy.model.LabBooking;
import com.example.onlinepharmacy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabBookingRepository extends JpaRepository<LabBooking, Long> {
    List<LabBooking> findByUserOrderByCreatedAtDesc(User user);
}
