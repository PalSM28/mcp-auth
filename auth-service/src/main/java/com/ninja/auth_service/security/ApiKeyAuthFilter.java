package com.ninja.auth_service.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ninja.auth_service.services.AuthenticationService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Autowired
    private AuthenticationService authService; // your isValid() service
    private static final String API_KEY_HEADER = "X-API-Key";
    
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    
   
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String apiKey = request.getHeader(API_KEY_HEADER);
        String clientIP = getClientIP(request);
        
        logger.info("Auth Service Request: {} {} from IP: {} with API Key: {}", 
                   method, requestURI, clientIP, apiKey != null ? "Present" : "Missing");
        
        // For /auth/validate-key endpoint, API key is required
        if (requestURI.equals("/auth/validate-key")) {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                logger.warn("Missing API key in validation request from IP: {}", clientIP);
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                                "API key is required in X-API-Key header");
                return;
            }
            
            // Basic API key format validation
            if (!isValidApiKeyFormat(apiKey)) {
                logger.warn("Invalid API key format from IP: {}", clientIP);
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                                "Invalid API key format");
                return;
            }
            
            // Set authentication context with API key for processing
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(apiKey, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        // Add security headers
        addSecurityHeaders(response);
        
        // Continue with filter chain
        filterChain.doFilter(request, response);
        
        // Log response
        logger.info("Auth Service Response: {} for {} {} from IP: {}", 
                   response.getStatus(), method, requestURI, clientIP);
    }

    private boolean isValidApiKeyFormat(String apiKey) {
        // Basic validation - API key should be at least 10 characters and contain alphanumeric + hyphens
        return apiKey != null && 
               apiKey.length() >= 10 && 
               apiKey.matches("^[a-zA-Z0-9-_]+$");
    }

    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) 
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
            "{\"error\":\"%s\",\"status\":%d,\"timestamp\":\"%s\"}", 
            message, status, java.time.Instant.now().toString()));
    }

    private String getClientIP(HttpServletRequest request) {
        // Check for IP in various headers (useful when behind load balancers/proxies)
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "X-Client-IP",
            "CF-Connecting-IP", // Cloudflare
            "True-Client-IP"    // Akamai
        };
        
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, get the first one
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Skip filter for health check and actuator endpoints
        return path.equals("/health") || 
               path.startsWith("/actuator/") || 
               path.equals("/favicon.ico");
    }
}


