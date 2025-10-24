package com.ecommerce.returnmanager.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ecommerce.returnmanager.model.ReturnRequest;
import com.ecommerce.returnmanager.repository.ReturnRequestRepository;

@Service
public class AnalyticsService {

    private final ReturnRequestRepository returnRequestRepository;

    public AnalyticsService(ReturnRequestRepository returnRequestRepository) {
        this.returnRequestRepository = returnRequestRepository;
    }

    public Map<String, Object> getDashboardMetrics() {
        List<ReturnRequest> allReturns = returnRequestRepository.findAll();
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Basic counts
        metrics.put("totalReturns", allReturns.size());
        metrics.put("pendingReturns", allReturns.stream()
                .filter(r -> r.getStatus() == ReturnRequest.ReturnStatus.PENDING).count());
        metrics.put("approvedReturns", allReturns.stream()
                .filter(r -> r.getStatus() == ReturnRequest.ReturnStatus.APPROVED).count());
        metrics.put("rejectedReturns", allReturns.stream()
                .filter(r -> r.getStatus() == ReturnRequest.ReturnStatus.REJECTED).count());
        metrics.put("refundedReturns", allReturns.stream()
                .filter(r -> r.getStatus() == ReturnRequest.ReturnStatus.REFUNDED).count());

        // Financial metrics
        BigDecimal totalRefundAmount = allReturns.stream()
                .filter(r -> r.getRefundAmount() != null)
                .map(ReturnRequest::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        metrics.put("totalRefundAmount", totalRefundAmount);

        // Recent activity (last 7 days)
        long recentReturns = allReturns.stream()
                .filter(r -> r.getRequestDate().isAfter(LocalDateTime.now().minusDays(7)))
                .count();
        metrics.put("recentReturns", recentReturns);

        // Return reasons breakdown
        Map<String, Long> reasonBreakdown = new HashMap<>();
        for (ReturnRequest.ReturnReason reason : ReturnRequest.ReturnReason.values()) {
            long count = allReturns.stream()
                    .filter(r -> r.getReason() == reason)
                    .count();
            reasonBreakdown.put(reason.toString(), count);
        }
        metrics.put("reasonBreakdown", reasonBreakdown);

        // Monthly trend (last 6 months)
        Map<String, Long> monthlyTrend = new HashMap<>();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime start = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0);
            LocalDateTime end = start.plusMonths(1);
            
            long monthlyCount = allReturns.stream()
                    .filter(r -> r.getRequestDate().isAfter(start) && r.getRequestDate().isBefore(end))
                    .count();
            
            String monthKey = start.getMonth().toString() + " " + start.getYear();
            monthlyTrend.put(monthKey, monthlyCount);
        }
        metrics.put("monthlyTrend", monthlyTrend);

        // Success rate (approved vs total)
        double approvalRate = allReturns.isEmpty() ? 0 : 
                (double) allReturns.stream()
                        .filter(r -> r.getStatus() == ReturnRequest.ReturnStatus.APPROVED || 
                                   r.getStatus() == ReturnRequest.ReturnStatus.REFUNDED)
                        .count() / allReturns.size() * 100;
        metrics.put("approvalRate", Math.round(approvalRate * 100.0) / 100.0);

        return metrics;
    }

    public Map<String, Object> getPerformanceMetrics() {
        List<ReturnRequest> allReturns = returnRequestRepository.findAll();
        
        Map<String, Object> performance = new HashMap<>();
        
        // Average processing time
        double avgProcessingHours = allReturns.stream()
                .filter(r -> r.getResolutionDate() != null && r.getRequestDate() != null)
                .mapToLong(r -> java.time.Duration.between(r.getRequestDate(), r.getResolutionDate()).toHours())
                .average()
                .orElse(0.0);
        performance.put("avgProcessingHours", Math.round(avgProcessingHours * 100.0) / 100.0);

        // Return rate (assuming we have order count - for demo we'll simulate)
        performance.put("estimatedReturnRate", "4.2%");
        performance.put("industryAverage", "5.8%");

        return performance;
    }
}