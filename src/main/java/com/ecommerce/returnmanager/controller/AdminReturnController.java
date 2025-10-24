package com.ecommerce.returnmanager.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.returnmanager.dto.ApiResponse;
import com.ecommerce.returnmanager.model.ReturnRequest;
import com.ecommerce.returnmanager.repository.ReturnRequestRepository;
import com.ecommerce.returnmanager.service.ReturnRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/returns")
@Tag(name = "Admin Return Management", description = "APIs for administrators to manage return requests")
public class AdminReturnController {

    private final ReturnRequestService returnRequestService;
    private final ReturnRequestRepository returnRequestRepository;

    public AdminReturnController(ReturnRequestService returnRequestService, ReturnRequestRepository rrr) {
        this.returnRequestService = returnRequestService;
        this.returnRequestRepository = rrr;
    }

    @Operation(
        summary = "Get all pending return requests",
        description = "Retrieve all return requests that are pending admin review"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pending requests retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ReturnRequest>>> getAllPendingReturns(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Log admin activity (optional - for future auditing)
        if (userDetails != null) {
            System.out.println("Admin " + userDetails.getUsername() + " viewed pending returns");
        }
        
        List<ReturnRequest> requests = returnRequestService.getAllPendingRequests();
        return ResponseEntity.ok(ApiResponse.success("Pending return requests retrieved successfully", requests));
    }
    
    @Operation(
        summary = "Approve a return request",
        description = "Approve a pending return request and process refund"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Return request approved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Return request not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - request not in pending status")
    })
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ReturnRequest>> approveReturn(
            @Parameter(description = "ID of the return request to approve") @PathVariable Long id, 
            @Parameter(description = "Admin notes for the approval") @RequestParam String notes,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ReturnRequest updatedRequest = returnRequestService.approveRequest(id, notes);
            
            // Log admin activity
            if (userDetails != null) {
                System.out.println("Admin " + userDetails.getUsername() + " approved return request ID: " + id);
            }
            
            return ResponseEntity.ok(ApiResponse.success("Return request approved and refund processed", updatedRequest));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Return request not found with ID: " + id));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Cannot approve request: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to approve request: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Reject a return request",
        description = "Reject a pending return request"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Return request rejected successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Return request not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - request not in pending status")
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ReturnRequest>> rejectReturn(
            @Parameter(description = "ID of the return request to reject") @PathVariable Long id, 
            @Parameter(description = "Reason for rejection") @RequestParam String notes,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ReturnRequest updatedRequest = returnRequestService.rejectRequest(id, notes);
            
            // Log admin activity
            if (userDetails != null) {
                System.out.println("Admin " + userDetails.getUsername() + " rejected return request ID: " + id);
            }
            
            return ResponseEntity.ok(ApiResponse.success("Return request rejected successfully", updatedRequest));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Return request not found with ID: " + id));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Cannot reject request: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to reject request: " + e.getMessage()));
        }
    }

    @Operation(
    summary = "Get all return requests with filtering",
    description = "Retrieve all return requests with optional status filtering"
)
@GetMapping
public ResponseEntity<ApiResponse<List<ReturnRequest>>> getAllReturns(
        @Parameter(description = "Filter by status") @RequestParam(required = false) ReturnRequest.ReturnStatus status,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    List<ReturnRequest> requests;
    if (status != null) {
        requests = returnRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == status)
                .collect(Collectors.toList());
    } else {
        requests = returnRequestRepository.findAll();
    }
    
    return ResponseEntity.ok(ApiResponse.success("Return requests retrieved successfully", requests));
}

@Operation(
    summary = "Get return statistics",
    description = "Retrieve statistics about return requests for admin dashboard"
)
@GetMapping("/statistics")
public ResponseEntity<ApiResponse<Map<String, Object>>> getReturnStatistics(
        @AuthenticationPrincipal UserDetails userDetails) {
    
    List<ReturnRequest> allRequests = returnRequestRepository.findAll();
    
    Map<String, Object> statistics = new HashMap<>();
    
    // Count by status
    statistics.put("total", allRequests.size());
    statistics.put("pending", allRequests.stream()
            .filter(r -> r.getStatus() == ReturnRequest.ReturnStatus.PENDING).count());
    statistics.put("approved", allRequests.stream()
            .filter(r -> r.getStatus() == ReturnRequest.ReturnStatus.APPROVED).count());
    statistics.put("rejected", allRequests.stream()
            .filter(r -> r.getStatus() == ReturnRequest.ReturnStatus.REJECTED).count());
    statistics.put("refunded", allRequests.stream()
            .filter(r -> r.getStatus() == ReturnRequest.ReturnStatus.REFUNDED).count());
    
    // Total refund amount
    BigDecimal totalRefundAmount = allRequests.stream()
            .filter(r -> r.getRefundAmount() != null)
            .map(ReturnRequest::getRefundAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    statistics.put("totalRefundAmount", totalRefundAmount);
    
    // Recent activity (last 7 days)
    long recentRequests = allRequests.stream()
            .filter(r -> r.getRequestDate().isAfter(LocalDateTime.now().minusDays(7)))
            .count();
    statistics.put("recentRequests", recentRequests);
    
    return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", statistics));
}
}