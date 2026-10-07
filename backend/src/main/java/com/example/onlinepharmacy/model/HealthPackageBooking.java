package com.example.onlinepharmacy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_package_bookings")
public class HealthPackageBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String packageName;
    private String patientName;
    private String bookingDate;
    private String bookingTime;
    private BigDecimal price;
    private String status;
    private LocalDateTime createdAt;

    public HealthPackageBooking() {}

    public HealthPackageBooking(User user, String packageName, String patientName,
                                String bookingDate, String bookingTime, BigDecimal price) {
        this.user = user;
        this.packageName = packageName;
        this.patientName = patientName;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.price = price;
        this.status = "CONFIRMED";
        this.createdAt = LocalDateTime.now();
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingTime() { return bookingTime; }
    public void setBookingTime(String bookingTime) { this.bookingTime = bookingTime; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
