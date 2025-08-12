package com.ninja.auth_service.utility;


import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ninja.auth_service.dto.AuthenticationResponse;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String API_KEY_HEADER = "X-API-Key";
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}") // 1 hour in milliseconds
    private Long jwtExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }	
    
    public String generateToken(String clientName, String apiKey) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        logger.info("Generating JWT token for client: {}", clientName);
        
        return Jwts.builder()
                .setSubject(clientName)
                .claim(API_KEY_HEADER, apiKey)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public String getClientNameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    public String getApiKeyFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return (String) claims.get("apiKey");
    }
    
    public AuthenticationResponse validateToken(String token) {
    	logger.info("Validating JWT token.");
    	String clientName = getClientNameFromToken(token);
        Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token);
        
        logger.info("Token validation successful for client: {}", clientName);
        return new AuthenticationResponse(token, getExpirationTime(),clientName);
    
    }
        /*catch (ExpiredJwtException ex) {
        }
            logger.error("JWT token is expired: {}", ex.getMessage());
            throw new com.microservices.common.exception.TokenExpiredException("JWT token is expired");
        } catch (UnsupportedJwtException ex) {
            logger.error("JWT token is unsupported: {}", ex.getMessage());
            throw new com.microservices.common.exception.InvalidTokenException("JWT token is unsupported");
        } catch (MalformedJwtException ex) {
            logger.error("JWT token is malformed: {}", ex.getMessage());
            throw new com.microservices.common.exception.InvalidTokenException("JWT token is malformed");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
            throw new com.microservices.common.exception.InvalidTokenException("JWT claims string is empty");
        } catch (Exception ex) {
            logger.error("JWT token validation failed: {}", ex.getMessage());
            throw new com.microservices.common.exception.InvalidTokenException("JWT token validation failed");
        }*/
    
    
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getExpiration();
    }
    
    public Long getExpirationTime() {
        return jwtExpiration;
    }
}
