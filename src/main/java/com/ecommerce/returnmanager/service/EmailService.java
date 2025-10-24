package com.ecommerce.returnmanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendReturnConfirmation(String customerEmail, Long requestId) {
        // In production, integrate with actual email service (SendGrid, AWS SES, etc.)
        String subject = "Return Request Confirmation - #" + requestId;
        String body = String.format("""
            Dear Customer,
            
            Your return request #%d has been received and is under review.
            
            We will process your request within 3-5 business days.
            
            Thank you,
            E-commerce Return Team
            """, requestId);
            
        logger.info("EMAIL_SENT | To: {} | Subject: {} | Body: {}", 
                   customerEmail, subject, body);
        
        // Simulate email sending
        System.out.println("=== EMAIL NOTIFICATION ===");
        System.out.println("To: " + customerEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println("=========================");
    }

    public void sendReturnApproval(String customerEmail, Long requestId, Double refundAmount) {
        String subject = "Return Approved - Refund Processed #" + requestId;
        String body = String.format("""
            Dear Customer,
            
            Great news! Your return request #%d has been approved.
            
            A refund of $%.2f has been processed and will reflect in your account within 5-7 business days.
            
            Thank you for shopping with us!
            
            Sincerely,
            E-commerce Return Team
            """, requestId, refundAmount);
            
        logger.info("APPROVAL_EMAIL_SENT | To: {} | RequestID: {} | Refund: ${}", 
                   customerEmail, requestId, refundAmount);
        
        System.out.println("=== REFUND APPROVAL EMAIL ===");
        System.out.println("To: " + customerEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Refund Amount: $" + refundAmount);
        System.out.println("=============================");
    }

    public void sendAdminAlert(String subject, String message) {
        logger.info("ADMIN_ALERT | Subject: {} | Message: {}", subject, message);
        
        System.out.println("=== ADMIN ALERT ===");
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        System.out.println("===================");
    }
}