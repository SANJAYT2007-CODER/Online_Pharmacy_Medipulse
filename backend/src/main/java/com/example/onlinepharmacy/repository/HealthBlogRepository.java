package com.example.onlinepharmacy.repository;

import com.example.onlinepharmacy.model.HealthBlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HealthBlogRepository extends JpaRepository<HealthBlog, Long> {
    List<HealthBlog> findByCategory(String category);
}
