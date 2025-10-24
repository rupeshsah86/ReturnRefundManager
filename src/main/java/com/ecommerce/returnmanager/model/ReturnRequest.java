package com.ecommerce.returnmanager.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "return_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔴 FIX 1: ADD THE MISSING order_id FIELD 🔴
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    @NotNull(message = "Return request must specify an order item")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Return request must be linked to a user")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    // ✅ Updated validation for quantityReturned
    @NotNull(message = "Quantity to return is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantityReturned;

    @Column(nullable = false)
    private LocalDateTime requestDate = LocalDateTime.now();

    private LocalDateTime resolutionDate; // When the request is approved/rejected

    @Enumerated(EnumType.STRING)
    private ReturnReason reason;

    @Enumerated(EnumType.STRING)
    private ReturnStatus status = ReturnStatus.PENDING;

    // Calculated fields
    private BigDecimal refundAmount;
    private String adminNotes;

    public enum ReturnReason {
        DAMAGED, WRONG_ITEM, DONT_WANT, TOO_LATE, OTHER
    }

    public enum ReturnStatus {
        PENDING, APPROVED, REJECTED, REFUNDED, CLOSED
    }
}
