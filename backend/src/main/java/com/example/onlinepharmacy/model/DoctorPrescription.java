package com.example.onlinepharmacy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctor_prescriptions")
public class DoctorPrescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rxId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String doctorName;
    private String doctorSpecialization;
    private String patientName;
    private String consultationDate;
    private String diagnosisNotes;
    private String imageUrl;
    private String status; // Pending Verification, Verified, Rejected, Expired

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "rx_id")
    private List<PrescriptionItem> items = new ArrayList<>();

    private LocalDateTime createdAt;

    public DoctorPrescription() {}

    public DoctorPrescription(User user, String doctorName, String doctorSpecialization,
                              String patientName, String consultationDate, String diagnosisNotes,
                              String status) {
        this.user = user;
        this.doctorName = doctorName;
        this.doctorSpecialization = doctorSpecialization;
        this.patientName = patientName;
        this.consultationDate = consultationDate;
        this.diagnosisNotes = diagnosisNotes;
        this.status = status != null ? status : "Verified";
        this.createdAt = LocalDateTime.now();
    }

    public Long getRxId() { return rxId; }
    public void setRxId(Long rxId) { this.rxId = rxId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String doctorSpecialization) { this.doctorSpecialization = doctorSpecialization; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getConsultationDate() { return consultationDate; }
    public void setConsultationDate(String consultationDate) { this.consultationDate = consultationDate; }

    public String getDiagnosisNotes() { return diagnosisNotes; }
    public void setDiagnosisNotes(String diagnosisNotes) { this.diagnosisNotes = diagnosisNotes; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<PrescriptionItem> getItems() { return items; }
    public void setItems(List<PrescriptionItem> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
