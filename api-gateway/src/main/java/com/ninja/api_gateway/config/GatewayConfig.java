package com.ninja.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ninja.api_gateway.security.AuthenticationGatewayFilterFactory;
import com.ninja.api_gateway.services.AuthenticationService;

@Configuration
public class GatewayConfig {
    	
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, AuthenticationGatewayFilterFactory authFilter) {
        return builder.routes()
                // Route for MCP Client with authentication
                .route("mcp-client-service", r -> r
                        .path("/mcp/**")
                        .filters(f -> f
                                .filter( authFilter.apply(
                                    new AuthenticationGatewayFilterFactory.Config()))
                                .stripPrefix(1) // Remove /mcp from path
                        )
                        .uri("lb://mcp-client")
                )
                
                // Route for Auth Service (no authentication required)
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .uri("lb://auth-service")
                )
                
                // Health check routes (no authentication)
                .route("health-route", r -> r
                        .path("/actuator/**")
                        .uri("no://op")
                )
                
                .build();
    }
}