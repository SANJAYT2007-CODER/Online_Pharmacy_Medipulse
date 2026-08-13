package com.example.onlinepharmacy.controller;

import com.example.onlinepharmacy.dto.OrderRequest;
import com.example.onlinepharmacy.model.Address;
import com.example.onlinepharmacy.model.Order;
import com.example.onlinepharmacy.model.User;
import com.example.onlinepharmacy.repository.AddressRepository;
import com.example.onlinepharmacy.repository.UserRepository;
import com.example.onlinepharmacy.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Autowired
    public OrderApiController(OrderService orderService, AddressRepository addressRepository, UserRepository userRepository) {
        this.orderService = orderService;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null ? user.getUserId() : 3L; // Fallback to Customer ID
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody OrderRequest request, HttpSession session) {
        try {
            Long userId = getCurrentUserId(session);
            Order order = orderService.checkout(userId, request);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<Order>> getMyOrders(HttpSession session) {
        Long userId = getCurrentUserId(session);
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            Order order = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<Address>> getAddresses(HttpSession session) {
        Long userId = getCurrentUserId(session);
        return ResponseEntity.ok(addressRepository.findByUser_UserId(userId));
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> addAddress(@RequestBody Address address, HttpSession session) {
        try {
            Long userId = getCurrentUserId(session);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            address.setUser(user);
            Address saved = addressRepository.save(address);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
