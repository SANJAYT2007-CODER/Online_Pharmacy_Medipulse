package com.example.onlinepharmacy.repository;

import com.example.onlinepharmacy.model.Order;
import com.example.onlinepharmacy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
    List<Order> findByUser_UserIdOrderByOrderDateDesc(Long userId);
    List<Order> findByOrderStatus(String orderStatus);
    List<Order> findAllByOrderByOrderDateDesc();
}
