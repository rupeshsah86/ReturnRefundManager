package com.ecommerce.returnmanager.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.returnmanager.model.Order;
import com.ecommerce.returnmanager.model.OrderItem;
import com.ecommerce.returnmanager.model.ReturnRequest;
import com.ecommerce.returnmanager.model.User;
import com.ecommerce.returnmanager.repository.OrderItemRepository;
import com.ecommerce.returnmanager.repository.OrderRepository;
import com.ecommerce.returnmanager.repository.ReturnRequestRepository;
import com.ecommerce.returnmanager.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReturnRequestServiceIntegrationTest {

    @Autowired
    private ReturnRequestService returnRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    private User testUser;
    private Order testOrder;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        returnRequestRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@test.com");
        testUser.setPassword("password");
        testUser.setRole(User.Role.CUSTOMER);
        testUser = userRepository.save(testUser);

        // Create test order
        testOrder = new Order();
        testOrder.setUser(testUser);
        testOrder.setOrderDate(LocalDateTime.now().minusDays(5)); // Within 30-day window
        testOrder.setStatus(Order.OrderStatus.DELIVERED);
        testOrder.setTotalAmount(100.00);
        testOrder = orderRepository.save(testOrder);

        // Create test order item
        testOrderItem = new OrderItem();
        testOrderItem.setProductName("Test Product");
        testOrderItem.setQuantity(2);
        testOrderItem.setPricePerItem(new BigDecimal("50.00"));
        testOrderItem.setOrder(testOrder);
        testOrderItem = orderItemRepository.save(testOrderItem);
    }

    @Test
    void initiateReturn_WithValidData_ShouldCreateReturnRequest() {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrderItem(testOrderItem);
        returnRequest.setUser(testUser);
        returnRequest.setOrderId(testOrder.getId()); // Set orderId to avoid constraint violation
        returnRequest.setQuantityReturned(1);
        returnRequest.setReason(ReturnRequest.ReturnReason.DAMAGED);

        ReturnRequest result = returnRequestService.initiateReturn(returnRequest);

        assertNotNull(result.getId());
        assertEquals(ReturnRequest.ReturnStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("50.00"), result.getRefundAmount());
        assertNotNull(result.getRequestDate());
    }

    @Test
    void getAllPendingRequests_ShouldReturnOnlyPendingRequests() {
        // Create a pending request
        ReturnRequest pendingRequest = new ReturnRequest();
        pendingRequest.setOrderItem(testOrderItem);
        pendingRequest.setUser(testUser);
        pendingRequest.setOrderId(testOrder.getId()); // Set orderId
        pendingRequest.setQuantityReturned(1);
        pendingRequest.setReason(ReturnRequest.ReturnReason.DAMAGED);
        pendingRequest.setStatus(ReturnRequest.ReturnStatus.PENDING);
        returnRequestRepository.save(pendingRequest);

        List<ReturnRequest> pendingRequests = returnRequestService.getAllPendingRequests();

        assertEquals(1, pendingRequests.size());
        assertEquals(ReturnRequest.ReturnStatus.PENDING, pendingRequests.get(0).getStatus());
    }
}