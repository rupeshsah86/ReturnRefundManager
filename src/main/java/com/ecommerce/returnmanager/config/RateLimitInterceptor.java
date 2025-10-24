package com.ecommerce.returnmanager.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final ConcurrentHashMap<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60; // 60 requests per minute
    private static final long TIME_WINDOW_MS = TimeUnit.MINUTES.toMillis(1);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String path = request.getRequestURI();
        
        // Skip rate limiting for admin endpoints (they have different limits)
        if (path.contains("/admin/")) {
            return true;
        }
        
        String key = clientIp + ":" + path;
        RequestCounter counter = requestCounts.computeIfAbsent(key, k -> new RequestCounter());
        
        if (counter.isRateLimited()) {
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("Rate limit exceeded. Please try again later.");
            return false;
        }
        
        counter.increment();
        return true;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
    
    private static class RequestCounter {
        private long windowStart;
        private int count;
        
        RequestCounter() {
            this.windowStart = System.currentTimeMillis();
            this.count = 0;
        }
        
        synchronized boolean isRateLimited() {
            long now = System.currentTimeMillis();
            if (now - windowStart > TIME_WINDOW_MS) {
                // Reset counter for new time window
                windowStart = now;
                count = 0;
                return false;
            }
            return count >= MAX_REQUESTS_PER_MINUTE;
        }
        
        synchronized void increment() {
            count++;
        }
    }
}