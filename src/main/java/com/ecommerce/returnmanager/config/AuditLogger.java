package com.ecommerce.returnmanager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {
    
    private static final Logger logger = LoggerFactory.getLogger("AUDIT");
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");

    public void logReturnCreation(Long requestId, String userEmail, String productName) {
        logger.info("RETURN_CREATED | RequestID: {} | User: {} | Product: {}", 
                   requestId, userEmail, productName);
    }

    public void logReturnApproval(Long requestId, String adminEmail, Double refundAmount) {
        logger.info("RETURN_APPROVED | RequestID: {} | Admin: {} | Refund: ${}", 
                   requestId, adminEmail, refundAmount);
    }

    public void logReturnRejection(Long requestId, String adminEmail, String reason) {
        logger.info("RETURN_REJECTED | RequestID: {} | Admin: {} | Reason: {}", 
                   requestId, adminEmail, reason);
    }

    public void logSecurityEvent(String event, String userEmail, String details) {
        securityLogger.warn("SECURITY_EVENT | Event: {} | User: {} | Details: {}", 
                          event, userEmail, details);
    }

    public void logPerformance(String operation, long durationMs) {
        performanceLogger.info("PERFORMANCE | Operation: {} | Duration: {}ms", 
                              operation, durationMs);
    }

    public void logError(String operation, String error, String userEmail) {
        logger.error("ERROR | Operation: {} | User: {} | Error: {}", 
                    operation, userEmail, error);
    }
}