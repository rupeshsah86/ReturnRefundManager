package com.ecommerce.returnmanager.controller;

import java.util.List;
import java.util.NoSuchElementException;

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

import com.ecommerce.returnmanager.model.ReturnRequest;
import com.ecommerce.returnmanager.service.ReturnRequestService;

@RestController
@RequestMapping("/api/v1/admin/returns")
public class AdminReturnController {

    private final ReturnRequestService returnRequestService;

    public AdminReturnController(ReturnRequestService returnRequestService) {
        this.returnRequestService = returnRequestService;
    }

    /**
     * GET /api/v1/admin/returns/pending
     * Admin views all PENDING return requests.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ReturnRequest>> getAllPendingReturns(@AuthenticationPrincipal UserDetails userDetails) {
        List<ReturnRequest> requests = returnRequestService.getAllPendingRequests();
        
        // Log admin activity (optional - for future auditing)
        if (userDetails != null) {
            System.out.println("Admin " + userDetails.getUsername() + " viewed pending returns");
        }
        
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }
    
    /**
     * PUT /api/v1/admin/returns/{id}/approve?notes={notes}
     * Admin approves a specific return request.
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveReturn(@PathVariable Long id, 
                                           @RequestParam String notes,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ReturnRequest updatedRequest = returnRequestService.approveRequest(id, notes);
            
            // Log admin activity (optional - for future auditing)
            if (userDetails != null) {
                System.out.println("Admin " + userDetails.getUsername() + " approved return request ID: " + id);
            }
            
            return new ResponseEntity<>(updatedRequest, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    /**
     * PUT /api/v1/admin/returns/{id}/reject?notes={notes}
     * Admin rejects a specific return request.
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectReturn(@PathVariable Long id, 
                                          @RequestParam String notes,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ReturnRequest updatedRequest = returnRequestService.rejectRequest(id, notes);
            
            // Log admin activity (optional - for future auditing)
            if (userDetails != null) {
                System.out.println("Admin " + userDetails.getUsername() + " rejected return request ID: " + id);
            }
            
            return new ResponseEntity<>(updatedRequest, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }
}