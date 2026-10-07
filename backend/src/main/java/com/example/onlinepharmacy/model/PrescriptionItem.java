package com.example.onlinepharmacy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "prescription_items")
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    private String medicineName;
    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;
    private String timing; // Before food / After food
    private BigDecimal price;
    private Long medicineId; // Linked medicine ID if available

    public PrescriptionItem() {}

    public PrescriptionItem(String medicineName, String dosage, String frequency, String duration,
                            String instructions, String timing, BigDecimal price, Long medicineId) {
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.instructions = instructions;
        this.timing = timing;
        this.price = price != null ? price : BigDecimal.valueOf(50);
        this.medicineId = medicineId;
    }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getTiming() { return timing; }
    public void setTiming(String timing) { this.timing = timing; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
}
