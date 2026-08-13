package com.example.onlinepharmacy.dto;

public class CartItemRequest {
    private Long medicineId;
    private Integer quantity;

    public CartItemRequest() {}

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
