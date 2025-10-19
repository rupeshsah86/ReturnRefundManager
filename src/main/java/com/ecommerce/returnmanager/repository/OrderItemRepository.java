package com.ecommerce.returnmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.returnmanager.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // No custom methods needed yet, basic CRUD provided by JpaRepository
}