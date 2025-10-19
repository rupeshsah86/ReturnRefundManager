package com.ecommerce.returnmanager.controller;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecommerce.returnmanager.model.ReturnRequest;
import com.ecommerce.returnmanager.model.ReturnRequest.ReturnReason;
import com.ecommerce.returnmanager.model.User;
import com.ecommerce.returnmanager.repository.UserRepository;
import com.ecommerce.returnmanager.service.ReturnRequestService;

@Controller
@RequestMapping("/")
public class WebController {

    private final ReturnRequestService returnRequestService;
    private final UserRepository userRepository; // <--- INJECTED FOR SIMULATION

    public WebController(ReturnRequestService returnRequestService, UserRepository userRepository) {
        this.returnRequestService = returnRequestService;
        this.userRepository = userRepository;
    }

    /**
     * Serves the return request form page.
     */
    @GetMapping
    public String showReturnForm(Model model) {
        model.addAttribute("returnRequest", new ReturnRequest());
        
        List<ReturnReason> reasons = Arrays.asList(ReturnReason.values());
        model.addAttribute("reasons", reasons);
        
        return "return-form"; // Refers to return-form.html
    }

    /**
     * Handles the form submission and processes the return.
     */
    @PostMapping("/submit-return")
    public String processReturn(@ModelAttribute("returnRequest") ReturnRequest request, Model model) {
        try {
            // FIX: SIMULATE AUTHENTICATION
            // Fetch the simulated customer (ID 1) before initiation.
            User customer = userRepository.findById(1L)
                .orElseThrow(() -> new NoSuchElementException("Simulated customer (ID 1) not found. Cannot process return."));
            
            request.setUser(customer); // <--- CRITICAL FIX: SET THE USER OBJECT
            
            ReturnRequest createdRequest = returnRequestService.initiateReturn(request);
            
            model.addAttribute("message", "Return Request Submitted Successfully!");
            model.addAttribute("details", createdRequest);
            return "return-success";

        } catch (Exception e) {
            model.addAttribute("error", "Failed to submit return: " + e.getMessage());
            // Re-add necessary model attributes for form reload
            model.addAttribute("returnRequest", request);
            model.addAttribute("reasons", Arrays.asList(ReturnReason.values()));
            return "return-form";
        }
    }
}
