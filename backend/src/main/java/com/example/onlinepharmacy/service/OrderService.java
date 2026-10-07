package com.example.onlinepharmacy.service;

import com.example.onlinepharmacy.dto.OrderRequest;
import com.example.onlinepharmacy.model.*;
import com.example.onlinepharmacy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
                        AddressRepository addressRepository, PrescriptionRepository prescriptionRepository,
                        MedicineRepository medicineRepository, UserRepository userRepository,
                        PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.medicineRepository = medicineRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    public Order checkout(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cannot place order with an empty cart");
        }

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Check if any medicine requires a prescription
        boolean rxRequired = cart.getCartItems().stream()
                .anyMatch(item -> Boolean.TRUE.equals(item.getMedicine().getRequiresPrescription()));

        Prescription prescription = null;
        if (rxRequired) {
            if (request.getPrescriptionId() == null) {
                throw new RuntimeException("Prescription is required for one or more medicines in your cart.");
            }
            prescription = prescriptionRepository.findById(request.getPrescriptionId())
                    .orElseThrow(() -> new RuntimeException("Selected prescription not found"));
        } else if (request.getPrescriptionId() != null) {
            prescription = prescriptionRepository.findById(request.getPrescriptionId()).orElse(null);
        }

        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cart.getCartItems()) {
            Medicine med = item.getMedicine();
            if (med.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + med.getMedicineName());
            }
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // Deduct stock quantity
            med.setStockQuantity(med.getStockQuantity() - item.getQuantity());
            medicineRepository.save(med);
        }

        Order order = new Order(user, address, prescription, totalAmount, "PLACED", "COMPLETED");

        for (CartItem cartItem : cart.getCartItems()) {
            BigDecimal subtotal = cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            OrderItem orderItem = new OrderItem(order, cartItem.getMedicine(), cartItem.getQuantity(),
                    cartItem.getPrice(), cartItem.getMedicine().getDiscount(), subtotal);
            order.getOrderItems().add(orderItem);
        }

        // Save order and order items
        Order savedOrder = orderRepository.save(order);

        // Process payment simulation
        String txId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment payment = new Payment(savedOrder, request.getPaymentMethod(), txId, totalAmount, "COMPLETED");
        paymentRepository.save(payment);
        savedOrder.setPayment(payment);

        // Clear cart
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUser_UserIdOrderByOrderDateDesc(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = getOrderById(orderId);
        order.setOrderStatus(newStatus);
        return orderRepository.save(order);
    }
}
