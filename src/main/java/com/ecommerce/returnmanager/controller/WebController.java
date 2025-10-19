package com.ecommerce.returnmanager.controller;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

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
    public String showReturnForm(Model model) {
        model.addAttribute("returnRequest", new ReturnRequest());
        
        List<ReturnReason> reasons = Arrays.asList(ReturnReason.values());
        model.addAttribute("reasons", reasons);
        
        return "return-form";
    }

    /**
     * Handles the customer form submission.
     */
    @PostMapping("/submit-return")
    public String processReturn(@ModelAttribute("returnRequest") ReturnRequest request, Model model) {
        try {
            // SIMULATE AUTHENTICATION
            User customer = userRepository.findById(1L)
                .orElseThrow(() -> new NoSuchElementException("Simulated customer (ID 1) not found. Cannot process return."));
            
            request.setUser(customer);
            
            ReturnRequest createdRequest = returnRequestService.initiateReturn(request);
            
            model.addAttribute("message", "Return Request Submitted Successfully!");
            model.addAttribute("details", createdRequest);
            return "return-success";

        } catch (Exception e) {
            model.addAttribute("error", "Failed to submit return: " + e.getMessage());
            model.addAttribute("returnRequest", request);
            model.addAttribute("reasons", Arrays.asList(ReturnReason.values()));
            return "return-form";
        }
    }

    // --- ADMIN CONTROLLER LOGIC STARTS HERE ---

    /**
     * Serves the admin dashboard showing all PENDING requests.
     */
    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        List<ReturnRequest> pendingRequests = returnRequestService.getAllPendingRequests();
        model.addAttribute("requests", pendingRequests);
        return "admin-dashboard";
    }

    /**
     * Handles the approval of a return request.
     */
    @PostMapping(value = {"/admin/approve/{id}", "/admin/approve/{id}/"}) 
    // We use RedirectAttributes to pass flash messages across the redirect.
    public String approveReturn(@PathVariable Long id, @RequestParam String adminNotes, RedirectAttributes redirectAttributes) {
        try {
            returnRequestService.approveRequest(id, adminNotes);
            redirectAttributes.addFlashAttribute("success", "Request ID " + id + " Approved and Refunded successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to approve request: " + e.getMessage());
        }
        // FIX: Changed from "forward:" to "redirect:" to prevent the 405 error after POST.
        return "redirect:/admin/dashboard"; 
    }

    /**
     * Handles the rejection of a return request.
     */
    @PostMapping(value = {"/admin/reject/{id}", "/admin/reject/{id}/"})
    // We use RedirectAttributes to pass flash messages across the redirect.
    public String rejectReturn(@PathVariable Long id, @RequestParam String adminNotes, RedirectAttributes redirectAttributes) {
        try {
            returnRequestService.rejectRequest(id, adminNotes);
            redirectAttributes.addFlashAttribute("success", "Request ID " + id + " Rejected successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reject request: " + e.getMessage());
        }
        // FIX: Changed from "forward:" to "redirect:" to prevent the 405 error after POST.
        return "redirect:/admin/dashboard"; 
    }
}
