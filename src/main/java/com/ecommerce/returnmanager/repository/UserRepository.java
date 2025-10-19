package com.ecommerce.returnmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.returnmanager.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // Custom find methods can be added here later (e.g., findByEmail)
}