// package com.ecommerce.returnmanager.controller;

// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;

// @Controller
// public class DashboardController {

//     @GetMapping("/dashboard")
//     public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
//         if (userDetails != null) {
//             boolean isAdmin = userDetails.getAuthorities().stream()
//                     .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            
//             // Add user info to model
//             model.addAttribute("username", userDetails.getUsername());
//             model.addAttribute("isAdmin", isAdmin);
            
//             if (isAdmin) {
//                 return "redirect:/admin/dashboard";
//             } else {
//                 return "customer-dashboard";
//             }
//         }
//         return "redirect:/login";
//     }
// }