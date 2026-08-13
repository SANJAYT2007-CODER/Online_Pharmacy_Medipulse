package com.example.onlinepharmacy.controller;

import com.example.onlinepharmacy.dto.CartItemRequest;
import com.example.onlinepharmacy.model.Cart;
import com.example.onlinepharmacy.model.User;
import com.example.onlinepharmacy.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private final CartService cartService;

    @Autowired
    public CartApiController(CartService cartService) {
        this.cartService = cartService;
    }

    private Long getCurrentUserId(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null ? user.getUserId() : 3L; // Default fallback to Customer account ID
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(HttpSession session) {
        Long userId = getCurrentUserId(session);
        return ResponseEntity.ok(cartService.getOrCreateCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItem(@RequestBody CartItemRequest request, HttpSession session) {
        try {
            Long userId = getCurrentUserId(session);
            Cart cart = cartService.addItemToCart(userId, request);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<?> updateItemQuantity(@PathVariable Long cartItemId,
                                                @RequestBody Map<String, Integer> body,
                                                HttpSession session) {
        try {
            Long userId = getCurrentUserId(session);
            Integer quantity = body.get("quantity");
            Cart cart = cartService.updateCartItemQuantity(userId, cartItemId, quantity);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<?> removeItem(@PathVariable Long cartItemId, HttpSession session) {
        try {
            Long userId = getCurrentUserId(session);
            Cart cart = cartService.removeCartItem(userId, cartItemId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
