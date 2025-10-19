package com.ecommerce.returnmanager.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping; // Used for simulation
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
    private final UserRepository userRepository; // For simulating user context

    public CustomerReturnController(ReturnRequestService returnRequestService, UserRepository userRepository) {
        this.returnRequestService = returnRequestService;
        this.userRepository = userRepository;
    }

    /**
     * POST /api/v1/customer/returns
     * Customer initiates a new return request.
     * * NOTE: In a real application, the User context would come from a security principal,
     * not a request parameter. We use a placeholder user ID (e.g., ID 1: Alice Customer).
     */
    @PostMapping
    public ResponseEntity<?> initiateReturn(@Valid @RequestBody ReturnRequest request) {
        try {
            // Simulation: Fetch the actual customer user (Alice is ID 1 from TestDataInitializer)
            // In a real system, you'd use @AuthenticationPrincipal
            Optional<User> userOptional = userRepository.findById(1L); 
            if (userOptional.isEmpty()) {
                return new ResponseEntity<>("Simulated customer user not found.", HttpStatus.FORBIDDEN);
            }
            request.setUser(userOptional.get());
            
            ReturnRequest createdRequest = returnRequestService.initiateReturn(request);
            
            return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * GET /api/v1/customer/returns/{id}
     * Customer views a specific return request.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReturnRequest> getReturnDetails(@PathVariable Long id) {
        // In a real app, you would verify the request belongs to the authenticated user.
        Optional<ReturnRequest> request = returnRequestService.getRequestById(id);
        
        return request.map(r -> new ResponseEntity<>(r, HttpStatus.OK))
                      .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}