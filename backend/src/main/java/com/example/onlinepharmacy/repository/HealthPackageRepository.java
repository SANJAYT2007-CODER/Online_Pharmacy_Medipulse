package com.example.onlinepharmacy.repository;

import com.example.onlinepharmacy.model.HealthPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthPackageRepository extends JpaRepository<HealthPackage, Long> {
}
