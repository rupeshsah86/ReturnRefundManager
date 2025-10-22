package com.ecommerce.returnmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.returnmanager.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // Custom find methods can be added here later (e.g., findByEmail)
    
    // Add this method for Spring Security authentication
    Optional<User> findByEmail(String email);
}