package com.ninja.api_gateway.services;


import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import reactor.util.retry.Retry;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.ninja.api_gateway.dto.AuthenticationRequest;
import com.ninja.api_gateway.dto.AuthenticationResponse;
import com.ninja.api_gateway.utility.JwtUtil;

import reactor.core.publisher.Mono;


@Service
public class AuthenticationService {
	
	private static final String API_KEY_HEADER = "X-API-Key";
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    @Value("${auth-service.url}")
    private String authServiceUrl;
    
    @Autowired
    private JwtUtil jwtUtil;

    //@Autowired
    private final WebClient webClient;

    public AuthenticationService() {
        this.webClient = WebClient.builder().build();
    }

    public Mono<AuthenticationResponse> validateApiKeyAndGenerateJWT(String apiKey) {
     
        Mono<AuthenticationResponse> tokenResponse = webClient.post()
                .uri(authServiceUrl + "/auth/validate-key")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(new AuthenticationRequest(apiKey))
                .retrieve()
                .bodyToMono(AuthenticationResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                    .filter(WebClientResponseException.class::isInstance))
                .doOnSuccess(response -> 
                    logger.info("Auth Service validated API key for client: {}", response.getClientName()))
                .doOnError(error -> 
                logger.error("Auth Service call failed: {}", error.getMessage()));
        
        return tokenResponse;
                /*.bodyValue(Map.of(API_KEY_HEADER, apiKey))
                
                .retrieve()
                .bodyToMono(AuthenticationResponse.class);
                //.onErrorResume(error -> {
                    // Fallback to local validation
                    //return validateApiKeyAndGenerateToken(apiKey);
                	return tokenResponse;
    */
                	
                	   /*try {
                    Mono<String> tokenMono = webClient.post()
                        .uri(authServiceUrl + "/auth/authenticate")
                        .header("x-api-Key", apiKey)
                        .retrieve()
                        .bodyToMono(String.class);
                    
                    return tokenMono.block();
                } catch (Exception e) {
                    return null;
                }*/
                
    }
    
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> jwtUtil.validateToken(token));
        
    }
    
}