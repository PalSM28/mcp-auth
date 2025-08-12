/**
 * 
 */
package com.ninja.api_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api")
public class GatewayController {

    @Value("${mcp-client-service.url}")
    private String mcpClientServiceUrl;

    //@Autowired
    private final WebClient webClient;

    public GatewayController() {
        this.webClient = WebClient.builder().build();
    }
    
    
    
    @GetMapping("/health")
    public Mono<ResponseEntity<Object>> health() {
        return Mono.just(ResponseEntity.ok("ApiGateway Service is UP!"));
    }


   /* @PostMapping("/diet-plan")
    public Mono<ResponseEntity<String>> generateDietPlan(@RequestBody String request, 
                                                        HttpServletRequest httpRequest) {
        String accessToken = (String) httpRequest.getAttribute("ACCESS_TOKEN");
        String apiKey = httpRequest.getHeader("x-api-key");
        
        return webClient.post()
            .uri(mcpClientServiceUrl + "/api/mcp/chat")
            .header("Authorization", "Bearer " + accessToken)
            .header("X-API-Key", apiKey)
            .bodyValue(request)
            .retrieve()
            .toEntity(String.class);
    }*/
}