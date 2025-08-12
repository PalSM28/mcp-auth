package com.ninja.api_gateway.security;

import java.util.ArrayList;
import java.util.Map;

import com.ninja.api_gateway.services.AuthenticationService;
import com.ninja.api_gateway.utility.JwtUtil;

import io.jsonwebtoken.io.IOException;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;

@Component
public class AuthenticationGatewayFilterFactory extends 
				AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

	private final AuthenticationService gatewayAuthService;
	
    public AuthenticationGatewayFilterFactory(AuthenticationService gatewayAuthService) {
        super(Config.class);
        this.gatewayAuthService = gatewayAuthService;
    }
   
    @Autowired
    private JwtUtil jwtUtil;
    
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String CLIENT_NAME_HEADER = "X-Client-Name";
    private static final String SCOPES_HEADER = "X-Scopes";
    
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationGatewayFilterFactory.class);

    @Override
    public GatewayFilter apply(Config config) {
    	 return (exchange, chain) -> {
		        ServerHttpRequest request = exchange.getRequest();
		        ServerHttpResponse response = exchange.getResponse();
		        // Step 1: Extract API Key
		        String apiKey = request.getHeaders().getFirst(API_KEY_HEADER);
		        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		
		     // Step 2: Validate API Key and Generate JWT
		        if (apiKey == null && apiKey.isEmpty()) {
		        	
		            logger.warn("Missing API Key");
		            return handleAuthenticationError(response, HttpStatus.UNAUTHORIZED, "Missing API Key");
		        
		        }
		        // First request with API key - validate and get token (REACTIVE)
		        return gatewayAuthService.validateApiKeyAndGenerateJWT(apiKey)
		                .flatMap(tokenResponse -> {
		                    // Correct way to mutate request in Spring Cloud Gateway
		                	// Step 3: Add JWT to downstream request headers
		                    ServerHttpRequest mutatedRequest = request.mutate()
		                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponse.getAccessToken())
		                            .header(API_KEY_HEADER, apiKey)
		                            .header(CLIENT_NAME_HEADER, tokenResponse.getClientName())
		                            .header(SCOPES_HEADER, String.join(",", tokenResponse.getScopes()))
		                            .build();
		
		                    logger.info("Authenticated client: {} with scopes: {}", 
		                             tokenResponse.getClientName(), tokenResponse.getScopes());
		                    
		                    ServerWebExchange mutatedExchange = exchange.mutate()
		                            .request(mutatedRequest)
		                            .build();
		                    
		                 // Step 4: Continue to downstream service
		                    return chain.filter(mutatedExchange);
		                })
		                .doOnSuccess(result -> 
		                logger.info("Authentication Successfully processed"))
		                .onErrorResume(error -> {
		                logger.error("Authentication failed: {}", error.getMessage());
		                return handleAuthenticationError(response, HttpStatus.UNAUTHORIZED, 
		                        "Authentication failed");
		            });
    	 };
    }

       
    private Mono<Void> handleAuthenticationError(ServerHttpResponse response, 
            HttpStatus status, String message) {
			response.setStatusCode(status);
			response.getHeaders().add("Content-Type", "application/json");
			
			String errorJson = String.format(
				"{\"error\": \"%s\", \"message\": \"%s\", \"timestamp\": \"%s\"}", 
				status.getReasonPhrase(), 
				message, 
				java.time.Instant.now()
			);
			
			var buffer = response.bufferFactory().wrap(errorJson.getBytes());
			return response.writeWith(Mono.just(buffer));
    }
    
    public static class Config {
        // Configuration properties can be added here if needed
    }
 }

    

