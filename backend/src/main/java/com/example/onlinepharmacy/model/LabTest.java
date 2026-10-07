package com.example.onlinepharmacy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lab_tests")
public class LabTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testId;

    @Column(nullable = false)
    private String testName;

    private String category;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 5, scale = 2)
    private BigDecimal discount;

    private String prepInstructions;
    private String imageUrl;
    private String status;

    public LabTest() {}

    public LabTest(String testName, String category, String description, BigDecimal price,
                   BigDecimal discount, String prepInstructions, String imageUrl) {
        this.testName = testName;
        this.category = category;
        this.description = description;
        this.price = price;
        this.discount = discount != null ? discount : BigDecimal.ZERO;
        this.prepInstructions = prepInstructions;
        this.imageUrl = imageUrl;
        this.status = "ACTIVE";
    }

    public Long getTestId() { return testId; }
    public void setTestId(Long testId) { this.testId = testId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public String getPrepInstructions() { return prepInstructions; }
    public void setPrepInstructions(String prepInstructions) { this.prepInstructions = prepInstructions; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
