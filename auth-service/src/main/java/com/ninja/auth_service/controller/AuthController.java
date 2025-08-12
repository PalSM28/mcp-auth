package com.ninja.auth_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ninja.auth_service.dto.AuthenticationResponse;
import com.ninja.auth_service.security.ApiKeyAuthFilter;
import com.ninja.auth_service.services.AuthenticationService;


@RestController
@RequestMapping("/auth")
public class AuthController {
	
	private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";
    @Autowired
    private AuthenticationService authenticationService;
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\"}");
    }
    @PostMapping("/validate-key")
    public ResponseEntity<AuthenticationResponse> validateApiKey(@RequestHeader(API_KEY_HEADER) String apiKey) 
    		throws Exception {
    	logger.debug("Received API key validation request");
        try {
            AuthenticationResponse response = authenticationService.validateApiKeyAndGenerateJwt(apiKey);//apiKey);
            
            if (response.isSuccess()) {
                logger.debug("API key validation successful for client: {}", response.getClientName());
            	
                return ResponseEntity.ok(response);
            } else {
                logger.warn("API key validation failed for client: {}", response.getClientName());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            logger.error("Error validating API key", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthenticationResponse(null, null,null));
        }
    }
    
    @PostMapping("/verify-token")
    public ResponseEntity<AuthenticationResponse> verifyToken(@RequestHeader("Authorization") String token) throws Exception {
        
    	logger.debug("Received JWT token verification request");
        
        try {
            // Remove "Bearer " prefix if present
            String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
            
            AuthenticationResponse response = authenticationService.verifyToken(jwt);
            
            if (response.isSuccess()) {
                logger.debug("JWT verification successful for client: {}", response.getClientName());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("JWT verification failed for client: {}", response.getClientName());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            logger.error("Error verifying JWT Token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthenticationResponse(null, null, null));
        }
    	
    	/*logger.info("Received token validation request");
        
        String token = extractTokenFromHeader(authHeader);
        boolean isValid = authenticationService.validateToken(token);
        String clientName = authenticationService.getClientNameFromToken(token);
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        response.put("clientName", clientName);
        
        logger.info("Token validation successful for client: {}", clientName);
        return ResponseEntity.ok(response);*/
    }
    
    private String extractTokenFromHeader(String authHeader) throws Exception {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
        //throw new Exception ("Invalid Authorization header format");
    }
    
    
    /*@PostMapping("/validate")
    public ResponseEntity<Map<String, String>> validateApiKey(@RequestHeader("x-api-key") String apiKey) {
        Optional<ApiKey> foundKey = apiKeyRepo.findByKeyAndActiveTrue(apiKey);

        if (foundKey.isPresent()) {
            String token = jwtUtil.generateToken(foundKey.get().getOwner(),apiKey);
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", token);
            response.put("apiKey", apiKey);
            response.put("secret", jwtUtil.getSecret());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid API Key"));
        }
    }*/

}
