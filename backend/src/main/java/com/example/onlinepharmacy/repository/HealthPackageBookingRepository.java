package com.example.onlinepharmacy.repository;

import com.example.onlinepharmacy.model.HealthPackageBooking;
import com.example.onlinepharmacy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HealthPackageBookingRepository extends JpaRepository<HealthPackageBooking, Long> {
    List<HealthPackageBooking> findByUserOrderByCreatedAtDesc(User user);
}
