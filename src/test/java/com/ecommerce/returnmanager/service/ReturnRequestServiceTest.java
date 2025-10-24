// package com.ecommerce.returnmanager.service;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import static org.mockito.ArgumentMatchers.any;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import org.mockito.junit.jupiter.MockitoExtension;

// import com.ecommerce.returnmanager.config.AuditLogger;  // Add this import
// import com.ecommerce.returnmanager.model.Order;
// import com.ecommerce.returnmanager.model.OrderItem;
// import com.ecommerce.returnmanager.model.ReturnRequest;
// import com.ecommerce.returnmanager.model.User;
// import com.ecommerce.returnmanager.repository.OrderItemRepository;
// import com.ecommerce.returnmanager.repository.ReturnRequestRepository;
// import com.ecommerce.returnmanager.service.impl.ReturnRequestServiceImpl;  // Add this import

// @ExtendWith(MockitoExtension.class)
// class ReturnRequestServiceTest {

//     @Mock
//     private ReturnRequestRepository returnRequestRepository;

//     @Mock
//     private OrderItemRepository orderItemRepository;

//     @Mock
//     private AuditLogger auditLogger;  // ADD THIS LINE

//     @Mock
//     private EmailService emailService;  // ADD THIS LINE

//     @InjectMocks
//     private ReturnRequestServiceImpl returnRequestService;

//     private User testUser;
//     private Order testOrder;
//     private OrderItem testOrderItem;
//     private ReturnRequest testReturnRequest;

//     @BeforeEach
//     void setUp() {
//     testUser = new User(1L, "Test User", "test@test.com", "password", User.Role.CUSTOMER);
    
//     testOrder = new Order();
//     testOrder.setId(1L);
//     testOrder.setUser(testUser);
//     testOrder.setOrderDate(LocalDateTime.now().minusDays(5));
//     testOrder.setStatus(Order.OrderStatus.DELIVERED);
    
//     testOrderItem = new OrderItem(1L, "Test Product", 2, new BigDecimal("50.00"), testOrder, null);
    
//     testReturnRequest = new ReturnRequest();
//     testReturnRequest.setId(1L);
//     testReturnRequest.setUser(testUser);
//     testReturnRequest.setOrderItem(testOrderItem);
//     testReturnRequest.setOrderId(1L); // Set orderId to avoid null constraint
//     testReturnRequest.setQuantityReturned(1);
//     testReturnRequest.setReason(ReturnRequest.ReturnReason.DAMAGED);
//     testReturnRequest.setStatus(ReturnRequest.ReturnStatus.PENDING);
//     testReturnRequest.setRefundAmount(new BigDecimal("50.00")); // ADD THIS LINE
// }

//     @Test
//     void initiateReturn_WithValidRequest_ShouldCreateReturn() {
//         when(orderItemRepository.findById(1L)).thenReturn(Optional.of(testOrderItem));
//         when(returnRequestRepository.save(any(ReturnRequest.class))).thenReturn(testReturnRequest);

//         ReturnRequest result = returnRequestService.initiateReturn(testReturnRequest);

//         assertNotNull(result);
//         assertEquals(ReturnRequest.ReturnStatus.PENDING, result.getStatus());
//         verify(returnRequestRepository, times(1)).save(any(ReturnRequest.class));
//     }

//     @Test
//     void getAllPendingRequests_ShouldReturnPendingRequests() {
//         ReturnRequest pendingRequest = new ReturnRequest();
//         pendingRequest.setStatus(ReturnRequest.ReturnStatus.PENDING);
        
//         ReturnRequest approvedRequest = new ReturnRequest();
//         approvedRequest.setStatus(ReturnRequest.ReturnStatus.APPROVED);

//         when(returnRequestRepository.findAll()).thenReturn(Arrays.asList(pendingRequest, approvedRequest));

//         List<ReturnRequest> result = returnRequestService.getAllPendingRequests();

//         assertFalse(result.isEmpty());
//         assertEquals(1, result.size());
//         assertEquals(ReturnRequest.ReturnStatus.PENDING, result.get(0).getStatus());
//     }

//     @Test
//     void approveRequest_WithPendingRequest_ShouldApprove() {
//         when(returnRequestRepository.findById(1L)).thenReturn(Optional.of(testReturnRequest));
//         when(returnRequestRepository.save(any(ReturnRequest.class))).thenReturn(testReturnRequest);

//         ReturnRequest result = returnRequestService.approveRequest(1L, "Approved for refund");

//         assertNotNull(result);
//         assertEquals(ReturnRequest.ReturnStatus.REFUNDED, result.getStatus());
//         assertNotNull(result.getResolutionDate());
//         assertEquals("Approved for refund", result.getAdminNotes());
//     }

//     @Test
//     void rejectRequest_WithPendingRequest_ShouldReject() {
//         when(returnRequestRepository.findById(1L)).thenReturn(Optional.of(testReturnRequest));
//         when(returnRequestRepository.save(any(ReturnRequest.class))).thenReturn(testReturnRequest);

//         ReturnRequest result = returnRequestService.rejectRequest(1L, "Item not eligible");

//         assertNotNull(result);
//         assertEquals(ReturnRequest.ReturnStatus.REJECTED, result.getStatus());
//         assertNotNull(result.getResolutionDate());
//         assertEquals("Item not eligible", result.getAdminNotes());
//     }
// }