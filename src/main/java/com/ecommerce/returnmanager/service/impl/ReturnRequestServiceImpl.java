package com.ecommerce.returnmanager.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.returnmanager.config.AuditLogger;
import com.ecommerce.returnmanager.model.Order;
import com.ecommerce.returnmanager.model.OrderItem;
import com.ecommerce.returnmanager.model.ReturnRequest;
import com.ecommerce.returnmanager.model.ReturnRequest.ReturnStatus;
import com.ecommerce.returnmanager.repository.OrderItemRepository;
import com.ecommerce.returnmanager.repository.ReturnRequestRepository;
import com.ecommerce.returnmanager.service.EmailService;
import com.ecommerce.returnmanager.service.ReturnRequestService;


@Service
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderItemRepository orderItemRepository;
    private final AuditLogger auditLogger;
    private final EmailService emailService;

    public ReturnRequestServiceImpl(ReturnRequestRepository returnRequestRepository, 
                                   OrderItemRepository orderItemRepository,
                                   AuditLogger auditLogger,
                                   EmailService emailService) {
        this.returnRequestRepository = returnRequestRepository;
        this.orderItemRepository = orderItemRepository;
        this.auditLogger = auditLogger;
        this.emailService = emailService;
    }

    private static final int RETURN_WINDOW_DAYS = 30;

    @Override
    @Transactional
    public ReturnRequest initiateReturn(ReturnRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. Validate Order Item Existence and link to Order
            Long orderItemId = request.getOrderItem().getId();
            OrderItem item = orderItemRepository.findById(orderItemId)
                    .orElseThrow(() -> new NoSuchElementException("Order Item not found with ID: " + orderItemId));
            
            Order order = item.getOrder();

            // Set the order ID
            request.setOrderId(order.getId()); 

            // 2. Business Logic: Check 30-day return window
            long daysSinceOrder = ChronoUnit.DAYS.between(order.getOrderDate(), LocalDateTime.now());
            if (daysSinceOrder > RETURN_WINDOW_DAYS) {
                request.setStatus(ReturnStatus.REJECTED);
                request.setAdminNotes("Request automatically rejected: Exceeded the " + RETURN_WINDOW_DAYS + "-day return window.");
                
                ReturnRequest rejectedRequest = returnRequestRepository.save(request);
                
                // Log automatic rejection
                auditLogger.logReturnRejection(
                    rejectedRequest.getId(), 
                    "system",
                    "Automatic rejection: Exceeded " + RETURN_WINDOW_DAYS + "-day return window"
                );
                
                return rejectedRequest;
            }
            
            // 3. Calculate Refund Amount (price per item * quantity returned)
            BigDecimal itemPrice = item.getPricePerItem();
            BigDecimal quantity = BigDecimal.valueOf(request.getQuantityReturned());
            BigDecimal refundAmount = itemPrice.multiply(quantity);
            
            // 4. Set Initial Status and Amounts
            request.setOrderItem(item);
            request.setRefundAmount(refundAmount);
            request.setStatus(ReturnStatus.PENDING);
            request.setRequestDate(LocalDateTime.now());

            ReturnRequest savedRequest = returnRequestRepository.save(request);
            
            // Log the action
            auditLogger.logReturnCreation(
                savedRequest.getId(), 
                request.getUser().getEmail(),
                request.getOrderItem().getProductName()
            );
            
            // Send confirmation email
            emailService.sendReturnConfirmation(
                request.getUser().getEmail(), 
                savedRequest.getId()
            );
            
            // Log performance
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.logPerformance("initiateReturn", duration);
            
            return savedRequest;
            
        } catch (Exception e) {
            auditLogger.logError("initiateReturn", e.getMessage(), 
                               request.getUser() != null ? request.getUser().getEmail() : "unknown");
            throw e;
        }
    }

    @Override
    public List<ReturnRequest> getAllPendingRequests() {
        long startTime = System.currentTimeMillis();
        
        try {
            List<ReturnRequest> requests = returnRequestRepository.findAll().stream()
                    .filter(r -> r.getStatus() == ReturnStatus.PENDING)
                    .collect(Collectors.toList());
            
            // Log performance
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.logPerformance("getAllPendingRequests", duration);
            
            return requests;
        } catch (Exception e) {
            auditLogger.logError("getAllPendingRequests", e.getMessage(), "system");
            throw e;
        }
    }

    @Override
    @Transactional
    public ReturnRequest approveRequest(Long requestId, String adminNotes) {
        long startTime = System.currentTimeMillis();
        
        try {
            ReturnRequest request = returnRequestRepository.findById(requestId)
                    .orElseThrow(() -> new NoSuchElementException("Return Request not found with ID: " + requestId));

            if (request.getStatus() != ReturnStatus.PENDING) {
                throw new IllegalStateException("Cannot approve a request that is not PENDING. Current status: " + request.getStatus());
            }

            request.setStatus(ReturnStatus.APPROVED);
            request.setResolutionDate(LocalDateTime.now());
            request.setAdminNotes(adminNotes);
            
            // In a real system, a refund process would be triggered here. 
            // For now, we simulate success and set status to REFUNDED immediately.
            request.setStatus(ReturnStatus.REFUNDED); 
            
            ReturnRequest updatedRequest = returnRequestRepository.save(request);
            
            // Log approval
            auditLogger.logReturnApproval(
                requestId, 
                "admin", // In real app, get from security context
                request.getRefundAmount().doubleValue()
            );
            
            // Send approval email to customer
            emailService.sendReturnApproval(
                request.getUser().getEmail(),
                requestId,
                request.getRefundAmount().doubleValue()
            );
            
            // Send admin alert for high-value refunds
            if (request.getRefundAmount().compareTo(new BigDecimal("500.00")) > 0) {
                emailService.sendAdminAlert(
                    "High-Value Refund Processed",
                    String.format("Refund of $%.2f processed for return request #%d", 
                                request.getRefundAmount().doubleValue(), requestId)
                );
            }
            
            // Log performance
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.logPerformance("approveRequest", duration);
            
            return updatedRequest;
            
        } catch (Exception e) {
            auditLogger.logError("approveRequest", e.getMessage(), "admin");
            throw e;
        }
    }

    @Override
    @Transactional
    public ReturnRequest rejectRequest(Long requestId, String adminNotes) {
        long startTime = System.currentTimeMillis();
        
        try {
            ReturnRequest request = returnRequestRepository.findById(requestId)
                    .orElseThrow(() -> new NoSuchElementException("Return Request not found with ID: " + requestId));

            if (request.getStatus() != ReturnStatus.PENDING) {
                throw new IllegalStateException("Cannot reject a request that is not PENDING. Current status: " + request.getStatus());
            }
            
            request.setStatus(ReturnStatus.REJECTED);
            request.setResolutionDate(LocalDateTime.now());
            request.setAdminNotes(adminNotes);

            ReturnRequest updatedRequest = returnRequestRepository.save(request);
            
            // Log rejection
            auditLogger.logReturnRejection(
                requestId, 
                "admin",
                adminNotes
            );
            
            // Log performance
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.logPerformance("rejectRequest", duration);
            
            return updatedRequest;
            
        } catch (Exception e) {
            auditLogger.logError("rejectRequest", e.getMessage(), "admin");
            throw e;
        }
    }

    @Override
    public Optional<ReturnRequest> getRequestById(Long id) {
        long startTime = System.currentTimeMillis();
        
        try {
            Optional<ReturnRequest> request = returnRequestRepository.findById(id);
            
            // Log performance
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.logPerformance("getRequestById", duration);
            
            return request;
        } catch (Exception e) {
            auditLogger.logError("getRequestById", e.getMessage(), "system");
            throw e;
        }
    }

    // Additional method for analytics
    public BigDecimal getTotalRefundsProcessed() {
        return returnRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReturnStatus.REFUNDED && r.getRefundAmount() != null)
                .map(ReturnRequest::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Additional method for dashboard statistics
    public long getReturnCountByStatus(ReturnStatus status) {
        return returnRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == status)
                .count();
    }
}