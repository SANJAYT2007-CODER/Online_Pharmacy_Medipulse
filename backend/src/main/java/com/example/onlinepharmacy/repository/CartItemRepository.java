package com.example.onlinepharmacy.repository;

import com.example.onlinepharmacy.model.Cart;
import com.example.onlinepharmacy.model.CartItem;
import com.example.onlinepharmacy.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndMedicine(Cart cart, Medicine medicine);
    void deleteByCart(Cart cart);
}
