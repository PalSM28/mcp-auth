package com.ninja.auth_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ninja.auth_service.security.ApiKeyAuthFilter;

import org.springframework.security.config.Customizer;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
	private ApiKeyAuthFilter apiKeyAuthFilter;
	
	private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
		
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		logger.info("Inside Auth Service Security Config:");
	    return http
	        //.cors(withDefaults())
	        .csrf(csrf -> csrf.disable())
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	       // .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/auth/validate-key").permitAll()
	            .requestMatchers("/actuator/**").permitAll()
	            .requestMatchers("/h2-console/**").permitAll()
	            .anyRequest().authenticated()
	        )
	        .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
	        .build();
	    
	}
	
	
}