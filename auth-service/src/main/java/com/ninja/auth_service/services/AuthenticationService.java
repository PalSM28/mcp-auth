package com.ninja.auth_service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ninja.auth_service.dto.AuthenticationResponse;
import com.ninja.auth_service.entity.ApiKey;
import com.ninja.auth_service.repository.ApiKeyRepository;
import com.ninja.auth_service.utility.JwtUtil;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthenticationService {
    
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
	
    @Autowired
    private ApiKeyRepository apiKeyRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthenticationResponse validateApiKeyAndGenerateJwt(String apiKey) throws Exception {
        
    	logger.debug("Validating API key and generating JWT");
    	// Find all active API keys and check hash
        try {
            Optional<ApiKey> apiKeyOpt = apiKeyRepository.findByKeyAndIsActiveTrue(apiKey);
            
            if (apiKeyOpt.isEmpty()) {
                throw new Exception("Invalid API key provided");
            }
            
            ApiKey apiKeyEntity = apiKeyOpt.get();
            logger.debug("API key validation successful for client: {}", apiKeyEntity.getClientName());
            // Check if API key is expired
            if (apiKeyEntity.getExpiresAt() != null && apiKeyEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
    
                throw new Exception("API key has expired");
            }
            
            // Generate JWT token             
            String token = jwtUtil.generateToken(apiKeyEntity.getClientName(), apiKeyEntity.getKey());
            logger.debug("Generate token successfully for clienty: {}", apiKeyEntity.getClientName());
            return new AuthenticationResponse(token, jwtUtil.getExpirationTime(),apiKeyEntity.getClientName());
            
        } catch (Exception ex) {
    
            throw ex;
        }
    }
    
    public AuthenticationResponse verifyToken(String token) {
        logger.debug("Validating JWT token");
        try {
        	String clientName = jwtUtil.getClientNameFromToken(token);
            AuthenticationResponse response = jwtUtil.validateToken(token);
            
            if(response.isSuccess()) {
            	logger.debug("JWT verification successful for client: {}", clientName);
            	return response;
            }
            else{ 
            	logger.debug("Token validation failed for client: {}", clientName);
            	return new AuthenticationResponse(null, null,null);
            }
        } catch (Exception ex) {
            logger.error("Token validation failed: {}", ex.getMessage());
            throw ex;
        }
    }
    
    public String getClientNameFromToken(String token) {
        return jwtUtil.getClientNameFromToken(token);
    }
}