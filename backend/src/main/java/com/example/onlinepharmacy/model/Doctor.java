package com.example.onlinepharmacy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorId;

    @Column(nullable = false)
    private String name;

    private String qualification;
    private String specialization;
    private Integer experienceYears;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal consultationFee;

    private String availableDate;
    private String availableTimeSlots; // comma separated
    private String photoUrl;
    private String status; // ACTIVE, INACTIVE

    public Doctor() {}

    public Doctor(String name, String qualification, String specialization, Integer experienceYears,
                  BigDecimal consultationFee, String availableDate, String availableTimeSlots,
                  String photoUrl, String status) {
        this.name = name;
        this.qualification = qualification;
        this.specialization = specialization;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee;
        this.availableDate = availableDate;
        this.availableTimeSlots = availableTimeSlots;
        this.photoUrl = photoUrl;
        this.status = status != null ? status : "ACTIVE";
    }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public String getAvailableDate() { return availableDate; }
    public void setAvailableDate(String availableDate) { this.availableDate = availableDate; }

    public String getAvailableTimeSlots() { return availableTimeSlots; }
    public void setAvailableTimeSlots(String availableTimeSlots) { this.availableTimeSlots = availableTimeSlots; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
