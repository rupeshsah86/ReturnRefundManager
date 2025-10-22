// package com.ecommerce.returnmanager.controller;

// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.GetMapping;

// @Controller
// public class DashboardController {

//     @GetMapping("/dashboard")
//     public String showDashboard(@AuthenticationPrincipal UserDetails userDetails) {
//         if (userDetails != null) {
//             // Check if user is admin
//             boolean isAdmin = userDetails.getAuthorities().stream()
//                     .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            
//             if (isAdmin) {
//                 return "redirect:/admin/dashboard";
//             } else {
//                 return "redirect:/";  // Redirect to return form for customers
//             }
//         }
//         // If not authenticated, redirect to login
//         return "redirect:/login";
//     }
// }