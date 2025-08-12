package com.ninja.api_gateway.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import com.ninja.api_gateway.security.AuthenticationGatewayFilterFactory;



@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Autowired
    private AuthenticationGatewayFilterFactory authenticationFilter;
	
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
            	.pathMatchers("/health", "/actuator/**").permitAll()
                .pathMatchers("/auth/**").permitAll() 
                //.pathMatchers("/api/mcp/**").permitAll()
                //.pathMatchers("/h2-console/**").permitAll()// Allow unauthenticated access to Auth Service
                .anyExchange().authenticated()        // All other routes require authentication
            )
            //.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
     
}
    



