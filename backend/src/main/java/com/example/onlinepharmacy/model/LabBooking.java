package com.example.onlinepharmacy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_bookings")
public class LabBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String testName;
    private String patientName;
    private String bookingDate;
    private String bookingTime;
    private BigDecimal totalPrice;
    private String status; // CONFIRMED, SAMPLE_COLLECTED, REPORT_READY
    private LocalDateTime createdAt;

    public LabBooking() {}

    public LabBooking(User user, String testName, String patientName, String bookingDate,
                      String bookingTime, BigDecimal totalPrice) {
        this.user = user;
        this.testName = testName;
        this.patientName = patientName;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.totalPrice = totalPrice;
        this.status = "CONFIRMED";
        this.createdAt = LocalDateTime.now();
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingTime() { return bookingTime; }
    public void setBookingTime(String bookingTime) { this.bookingTime = bookingTime; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
