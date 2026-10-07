package com.example.onlinepharmacy.dto;

public class OrderRequest {
    private Long addressId;
    private Long prescriptionId;
    private String paymentMethod; // CARD, UPI, NET_BANKING, COD

    public OrderRequest() {}

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
