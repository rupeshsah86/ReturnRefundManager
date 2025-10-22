package com.ecommerce.returnmanager.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.returnmanager.model.ReturnRequest;
import com.ecommerce.returnmanager.model.User;
import com.ecommerce.returnmanager.repository.UserRepository;
import com.ecommerce.returnmanager.service.ReturnRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customer/returns")
public class CustomerReturnController {

    private final ReturnRequestService returnRequestService;
    private final UserRepository userRepository;

    public CustomerReturnController(ReturnRequestService returnRequestService, UserRepository userRepository) {
        this.returnRequestService = returnRequestService;
        this.userRepository = userRepository;
    }

    /**
     * POST /api/v1/customer/returns
     * Customer initiates a new return request.
     */
    @PostMapping
    public ResponseEntity<?> initiateReturn(@Valid @RequestBody ReturnRequest request,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Use authenticated user instead of hardcoded ID
            if (userDetails == null) {
                return new ResponseEntity<>("User not authenticated. Please log in to submit a return request.", HttpStatus.UNAUTHORIZED);
            }
            
            User customer = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."));
            
            request.setUser(customer);
            
            ReturnRequest createdRequest = returnRequestService.initiateReturn(request);
            
            return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * GET /api/v1/customer/returns/{id}
     * Customer views a specific return request.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReturnDetails(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Verify authentication
            if (userDetails == null) {
                return new ResponseEntity<>("User not authenticated.", HttpStatus.UNAUTHORIZED);
            }
            
            Optional<ReturnRequest> request = returnRequestService.getRequestById(id);
            
            if (request.isEmpty()) {
                return new ResponseEntity<>("Return request not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            
            // Security check: Verify the return request belongs to the authenticated user
            ReturnRequest returnRequest = request.get();
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
            
            if (!returnRequest.getUser().getId().equals(currentUser.getId())) {
                return new ResponseEntity<>("Access denied. This return request does not belong to you.", HttpStatus.FORBIDDEN);
            }
            
            return new ResponseEntity<>(returnRequest, HttpStatus.OK);
            
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}