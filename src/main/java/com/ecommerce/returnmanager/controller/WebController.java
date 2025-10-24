package com.ecommerce.returnmanager.controller;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    /**
     * Serves the return request form page.
     */
    @GetMapping
    public String showReturnForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("returnRequest", new ReturnRequest());
        
        List<ReturnReason> reasons = Arrays.asList(ReturnReason.values());
        model.addAttribute("reasons", reasons);
        
        // Add username and role info to model if user is authenticated
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
            
            // Check if user is admin for UI display
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
        }
        
        return "return-form";
    }

    /**
     * Handles the customer form submission.
     */
    @PostMapping("/submit-return")
    public String processReturn(@ModelAttribute("returnRequest") ReturnRequest request, 
                               Model model, 
                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Use authenticated user instead of hardcoded ID
            if (userDetails == null) {
                throw new SecurityException("User not authenticated. Please log in to submit a return request.");
            }
            
            User customer = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found. Cannot process return."));
            
            request.setUser(customer);
            
            ReturnRequest createdRequest = returnRequestService.initiateReturn(request);
            
            model.addAttribute("message", "Return Request Submitted Successfully!");
            model.addAttribute("details", createdRequest);
            
            // Add username to success page
            if (userDetails != null) {
                model.addAttribute("username", userDetails.getUsername());
                
                // Check if user is admin for UI display
                boolean isAdmin = userDetails.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isAdmin", isAdmin);
            }
            
            return "return-success";

        } catch (Exception e) {
            model.addAttribute("error", "Failed to submit return: " + e.getMessage());
            model.addAttribute("returnRequest", request);
            model.addAttribute("reasons", Arrays.asList(ReturnReason.values()));
            
            // Add username back to form in case of error
            if (userDetails != null) {
                model.addAttribute("username", userDetails.getUsername());
                
                // Check if user is admin for UI display
                boolean isAdmin = userDetails.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isAdmin", isAdmin);
            }
            
            return "return-form";
        }
    }

    // --- ADMIN CONTROLLER LOGIC STARTS HERE ---

    /**
     * Serves the admin dashboard showing all PENDING requests.
     */
    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<ReturnRequest> pendingRequests = returnRequestService.getAllPendingRequests();
        model.addAttribute("requests", pendingRequests);
        
        // Add admin username to model
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
            model.addAttribute("isAdmin", true); // This is admin page, so always true
        }
        
        return "admin-dashboard";
    }

    /**
     * Handles the approval of a return request.
     */
    @PostMapping(value = {"/admin/approve/{id}", "/admin/approve/{id}/"}) 
    public String approveReturn(@PathVariable Long id, 
                               @RequestParam String adminNotes, 
                               RedirectAttributes redirectAttributes,
                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            returnRequestService.approveRequest(id, adminNotes);
            redirectAttributes.addFlashAttribute("success", "Request ID " + id + " Approved and Refunded successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to approve request: " + e.getMessage());
        }
        return "redirect:/admin/dashboard"; 
    }

    /**
     * Handles the rejection of a return request.
     */
    @PostMapping(value = {"/admin/reject/{id}", "/admin/reject/{id}/"})
    public String rejectReturn(@PathVariable Long id, 
                              @RequestParam String adminNotes, 
                              RedirectAttributes redirectAttributes,
                              @AuthenticationPrincipal UserDetails userDetails) {
        try {
            returnRequestService.rejectRequest(id, adminNotes);
            redirectAttributes.addFlashAttribute("success", "Request ID " + id + " Rejected successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reject request: " + e.getMessage());
        }
        return "redirect:/admin/dashboard"; 
    }
    @GetMapping("/analytics/dashboard")
    public String showAnalyticsDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails != null) {
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("isAdmin", true);
    }
    return "analytics-dashboard";
}
}