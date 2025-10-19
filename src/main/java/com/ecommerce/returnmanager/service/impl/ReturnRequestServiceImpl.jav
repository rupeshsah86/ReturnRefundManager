package com.ecommerce.returnmanager.service.impl;

import com.ecommerce.returnmanager.model.Order;
import com.ecommerce.returnmanager.model.OrderItem;
import com.ecommerce.returnmanager.model.ReturnRequest;
import com.ecommerce.returnmanager.model.ReturnRequest.ReturnStatus;
import com.ecommerce.returnmanager.repository.OrderItemRepository;
import com.ecommerce.returnmanager.repository.ReturnRequestRepository;
import com.ecommerce.returnmanager.service.ReturnRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderItemRepository orderItemRepository;

    public ReturnRequestServiceImpl(ReturnRequestRepository returnRequestRepository, OrderItemRepository orderItemRepository) {
        this.returnRequestRepository = returnRequestRepository;
        this.orderItemRepository = orderItemRepository;
    }

    private static final int RETURN_WINDOW_DAYS = 30;

    @Override
    @Transactional
    public ReturnRequest initiateReturn(ReturnRequest request) {
        // 1. Validate Order Item Existence and link to Order
        Long orderItemId = request.getOrderItem().getId();
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NoSuchElementException("Order Item not found with ID: " + orderItemId));
        
        Order order = item.getOrder();

        // 2. Business Logic: Check 30-day return window
        long daysSinceOrder = ChronoUnit.DAYS.between(order.getOrderDate(), LocalDateTime.now());
        if (daysSinceOrder > RETURN_WINDOW_DAYS) {
            request.setStatus(ReturnStatus.REJECTED);
            request.setAdminNotes("Request automatically rejected: Exceeded the " + RETURN_WINDOW_DAYS + "-day return window.");
            return returnRequestRepository.save(request);
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

        return returnRequestRepository.save(request);
    }

    @Override
    public List<ReturnRequest> getAllPendingRequests() {
        return returnRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReturnStatus.PENDING)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReturnRequest approveRequest(Long requestId, String adminNotes) {
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
        
        return returnRequestRepository.save(request);
    }

    @Override
    @Transactional
    public ReturnRequest rejectRequest(Long requestId, String adminNotes) {
        ReturnRequest request = returnRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Return Request not found with ID: " + requestId));

        if (request.getStatus() != ReturnStatus.PENDING) {
            throw new IllegalStateException("Cannot reject a request that is not PENDING. Current status: " + request.getStatus());
        }
        
        request.setStatus(ReturnStatus.REJECTED);
        request.setResolutionDate(LocalDateTime.now());
        request.setAdminNotes(adminNotes);

        return returnRequestRepository.save(request);
    }

    @Override
    public Optional<ReturnRequest> getRequestById(Long id) {
        return returnRequestRepository.findById(id);
    }
}