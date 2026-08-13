package com.example.onlinepharmacy.service;

import com.example.onlinepharmacy.dto.CartItemRequest;
import com.example.onlinepharmacy.model.Cart;
import com.example.onlinepharmacy.model.CartItem;
import com.example.onlinepharmacy.model.Medicine;
import com.example.onlinepharmacy.model.User;
import com.example.onlinepharmacy.repository.CartItemRepository;
import com.example.onlinepharmacy.repository.CartRepository;
import com.example.onlinepharmacy.repository.MedicineRepository;
import com.example.onlinepharmacy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       MedicineRepository medicineRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.medicineRepository = medicineRepository;
        this.userRepository = userRepository;
    }

    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUser_UserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            Cart newCart = new Cart(user);
            return cartRepository.save(newCart);
        });
    }

    public Cart addItemToCart(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new RuntimeException("Medicine not found with ID: " + request.getMedicineId()));

        if (medicine.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock available.");
        }

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndMedicine(cart, medicine);

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setPrice(medicine.getPrice());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(cart, medicine, request.getQuantity(), medicine.getPrice());
            cart.getCartItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return cartRepository.findById(cart.getCartId()).orElse(cart);
    }

    public Cart updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
        return cartRepository.findById(cart.getCartId()).orElse(cart);
    }

    public Cart removeCartItem(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        return cartRepository.findById(cart.getCartId()).orElse(cart);
    }

    public void clearCart(Cart cart) {
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }
}
