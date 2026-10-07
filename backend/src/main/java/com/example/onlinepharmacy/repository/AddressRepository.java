package com.example.onlinepharmacy.repository;

import com.example.onlinepharmacy.model.Address;
import com.example.onlinepharmacy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
    List<Address> findByUser_UserId(Long userId);
    Optional<Address> findByUser_UserIdAndIsDefaultTrue(Long userId);
}
