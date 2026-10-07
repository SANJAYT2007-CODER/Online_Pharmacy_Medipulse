package com.example.onlinepharmacy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "health_packages")
public class HealthPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packageId;

    @Column(nullable = false)
    private String packageName;

    @Column(columnDefinition = "TEXT")
    private String includedTests; // comma or bullet separated list

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    private String imageUrl;

    public HealthPackage() {}

    public HealthPackage(String packageName, String includedTests, String description,
                         BigDecimal originalPrice, BigDecimal discountedPrice, String imageUrl) {
        this.packageName = packageName;
        this.includedTests = includedTests;
        this.description = description;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.imageUrl = imageUrl;
    }

    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getIncludedTests() { return includedTests; }
    public void setIncludedTests(String includedTests) { this.includedTests = includedTests; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public BigDecimal getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(BigDecimal discountedPrice) { this.discountedPrice = discountedPrice; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
