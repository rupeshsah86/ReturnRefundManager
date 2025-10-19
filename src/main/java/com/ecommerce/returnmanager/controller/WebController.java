package com.ecommerce.returnmanager.controller;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable; // <--- NEW IMPORT
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // <--- NEW IMPORT

import com.ecommerce.returnmanager.model.ReturnRequest;
import com.ecommerce.returnmanager.model.ReturnRequest.ReturnReason;
import com.ecommerce.returnmanager.model.User;
import com.ecommerce.returnmanager.repository.UserRepository;
import com.ecommerce.returnmanager.service.ReturnRequestService;

@Controller
@RequestMapping("/")
public class WebController {

    private final ReturnRequestService returnRequestService;
    private final UserRepository userRepository; 

    public WebController(ReturnRequestService returnRequestService, UserRepository userRepository) {
        this.returnRequestService = returnRequestService;
        this.userRepository = userRepository;
    }

    // --- CUSTOMER UI METHODS ---

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
            // FIX: SIMULATE AUTHENTICATION (Customer ID 1)
            User customer = userRepository.findById(1L)
                .orElseThrow(() -> new NoSuchElementException("Simulated customer (ID 1) not found. Cannot process return."));
            
            request.setUser(customer); 
            
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
    
    // --- ADMIN UI METHODS (NEW) ---
    
    /**
     * Serves the admin dashboard showing all PENDING requests.
     */
    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        List<ReturnRequest> pendingRequests = returnRequestService.getAllPendingRequests();
        model.addAttribute("requests", pendingRequests);
        return "admin-dashboard"; // Refers to admin-dashboard.html
    }

    /**
     * Handles the approval of a return request.
     */
    @PostMapping("/admin/approve/{id}")
    public String approveReturn(@PathVariable Long id, @RequestParam String adminNotes, Model model) {
        try {
            returnRequestService.approveRequest(id, adminNotes);
            model.addAttribute("success", "Request ID " + id + " Approved and Refunded successfully.");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to approve request: " + e.getMessage());
        }
        return "forward:/admin/dashboard"; // Redirect back to the dashboard
    }

    /**
     * Handles the rejection of a return request.
     */
    @PostMapping("/admin/reject/{id}")
    public String rejectReturn(@PathVariable Long id, @RequestParam String adminNotes, Model model) {
        try {
            returnRequestService.rejectRequest(id, adminNotes);
            model.addAttribute("success", "Request ID " + id + " Rejected successfully.");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to reject request: " + e.getMessage());
        }
        return "forward:/admin/dashboard"; // Redirect back to the dashboard
    }
}
