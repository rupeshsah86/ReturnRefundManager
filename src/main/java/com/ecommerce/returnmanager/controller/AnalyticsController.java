package com.ecommerce.returnmanager.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.returnmanager.dto.ApiResponse;
import com.ecommerce.returnmanager.service.AnalyticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Real-time analytics and dashboard metrics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "Get dashboard metrics", description = "Retrieve comprehensive metrics for admin dashboard")
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardMetrics() {
        try {
            Map<String, Object> metrics = analyticsService.getDashboardMetrics();
            return ResponseEntity.ok(ApiResponse.success("Dashboard metrics retrieved successfully", metrics));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve dashboard metrics: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get performance metrics", description = "Retrieve performance and efficiency metrics")
    @GetMapping("/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPerformanceMetrics() {
        try {
            Map<String, Object> performance = analyticsService.getPerformanceMetrics();
            return ResponseEntity.ok(ApiResponse.success("Performance metrics retrieved successfully", performance));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve performance metrics: " + e.getMessage()));
        }
    }
}