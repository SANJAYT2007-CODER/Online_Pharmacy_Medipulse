package com.example.onlinepharmacy.repository;

import com.example.onlinepharmacy.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByStatus(String status);
    List<Medicine> findByCategory_CategoryIdAndStatus(Long categoryId, String status);

    @Query("SELECT m FROM Medicine m WHERE m.status = 'ACTIVE' AND " +
           "(LOWER(m.medicineName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Medicine> searchMedicines(@Param("query") String query);
}
