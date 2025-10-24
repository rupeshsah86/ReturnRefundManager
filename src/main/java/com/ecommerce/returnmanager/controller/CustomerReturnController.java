package com.ecommerce.returnmanager.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.returnmanager.dto.ApiResponse;
import com.ecommerce.returnmanager.model.ReturnRequest;
import com.ecommerce.returnmanager.model.User;
import com.ecommerce.returnmanager.repository.ReturnRequestRepository;
import com.ecommerce.returnmanager.repository.UserRepository;
import com.ecommerce.returnmanager.service.ReturnRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customer/returns")
@Tag(name = "Customer Return Management", description = "APIs for customers to manage their return requests")
public class CustomerReturnController {

    private final ReturnRequestService returnRequestService;
    private final ReturnRequestRepository returnRequestRepository;
    private final UserRepository userRepository;

    public CustomerReturnController(ReturnRequestService returnRequestService, UserRepository userRepository, ReturnRequestRepository rrr) {
        this.returnRequestService = returnRequestService;
        this.userRepository = userRepository;
        this.returnRequestRepository = rrr;
    }

    @Operation(
        summary = "Initiate a return request",
        description = "Allows customers to submit a new return request for an order item"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Return request created successfully", 
                    content = @Content(schema = @Schema(implementation = ReturnRequest.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user not authorized")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ReturnRequest>> initiateReturn(
            @Parameter(description = "Return request details") 
            @Valid @RequestBody ReturnRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated. Please log in to submit a return request."));
            }
            
            User customer = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."));
            
            request.setUser(customer);
            
            ReturnRequest createdRequest = returnRequestService.initiateReturn(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Return request submitted successfully", createdRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred: " + e.getMessage()));
        }
    }
    
    @Operation(
        summary = "Get return request details",
        description = "Retrieve details of a specific return request by ID"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Return request found", 
                    content = @Content(schema = @Schema(implementation = ReturnRequest.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user cannot access this return request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Return request not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnRequest>> getReturnDetails(
            @Parameter(description = "ID of the return request") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated."));
            }
            
            Optional<ReturnRequest> request = returnRequestService.getRequestById(id);
            
            if (request.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Return request not found with ID: " + id));
            }
            
            // Security check: Verify the return request belongs to the authenticated user
            ReturnRequest returnRequest = request.get();
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
            
            if (!returnRequest.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied. This return request does not belong to you."));
            }
            
            return ResponseEntity.ok(ApiResponse.success("Return request retrieved successfully", returnRequest));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred: " + e.getMessage()));
        }
    }

    @Operation(
    summary = "Get customer's return history",
    description = "Retrieve all return requests for the authenticated customer"
)
@GetMapping("/history")
public ResponseEntity<ApiResponse<List<ReturnRequest>>> getCustomerReturnHistory(
        @AuthenticationPrincipal UserDetails userDetails) {
    try {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated."));
        }
        
        User customer = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        
        List<ReturnRequest> returnRequests = returnRequestRepository.findByUserId(customer.getId());
        
        return ResponseEntity.ok(ApiResponse.success("Return history retrieved successfully", returnRequests));
        
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Failed to retrieve return history: " + e.getMessage()));
    }
}

@Operation(
    summary = "Cancel a pending return request",
    description = "Allows customers to cancel their own pending return requests"
)
@PutMapping("/{id}/cancel")
public ResponseEntity<ApiResponse<ReturnRequest>> cancelReturnRequest(
        @Parameter(description = "ID of the return request to cancel") @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
    try {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated."));
        }
        
        User customer = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        
        ReturnRequest returnRequest = returnRequestRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Return request not found with ID: " + id));
        
        // Security check: Verify the return request belongs to the authenticated user
        if (!returnRequest.getUser().getId().equals(customer.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. This return request does not belong to you."));
        }
        
        // Business rule: Only pending requests can be cancelled
        if (returnRequest.getStatus() != ReturnRequest.ReturnStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Cannot cancel request. Only pending requests can be cancelled."));
        }
        
        returnRequest.setStatus(ReturnRequest.ReturnStatus.CLOSED);
        returnRequest.setAdminNotes("Cancelled by customer");
        ReturnRequest updatedRequest = returnRequestRepository.save(returnRequest);
        
        return ResponseEntity.ok(ApiResponse.success("Return request cancelled successfully", updatedRequest));
        
    } catch (NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Failed to cancel return request: " + e.getMessage()));
    }
}

@Operation(
    summary = "Track return request status",
    description = "Get current status and timeline of a return request"
)
@GetMapping("/{id}/track")
public ResponseEntity<ApiResponse<Map<String, Object>>> trackReturnStatus(
        @Parameter(description = "ID of the return request") @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
    try {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated."));
        }
        
        ReturnRequest returnRequest = returnRequestRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Return request not found with ID: " + id));
        
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        
        // Security check
        if (!returnRequest.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. This return request does not belong to you."));
        }
        
        Map<String, Object> trackingInfo = new HashMap<>();
        trackingInfo.put("requestId", returnRequest.getId());
        trackingInfo.put("status", returnRequest.getStatus());
        trackingInfo.put("requestDate", returnRequest.getRequestDate());
        trackingInfo.put("resolutionDate", returnRequest.getResolutionDate());
        trackingInfo.put("estimatedTimeline", "3-5 business days for processing");
        
        if (returnRequest.getStatus() == ReturnRequest.ReturnStatus.APPROVED) {
            trackingInfo.put("nextSteps", "Refund will be processed within 24 hours");
        } else if (returnRequest.getStatus() == ReturnRequest.ReturnStatus.PENDING) {
            trackingInfo.put("nextSteps", "Under review by our team");
        }
        
        return ResponseEntity.ok(ApiResponse.success("Return status tracked successfully", trackingInfo));
        
    } catch (NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Failed to track return status: " + e.getMessage()));
    }
}
}