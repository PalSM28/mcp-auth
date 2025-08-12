package com.ninja.api_gateway.utility;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

	@Value("${jwt.secret:Dbd+9PYdWRN4l8OaX+oJrL5X95SCky8uw6HYPKkzGVs3z7OfIpCKr3qYoTVy7nZW/4h4uUyutak6G0kp1y3qRwQ==}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public boolean isValidJwtFormat(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }
            
            String[] parts = token.split("\\.");
            return parts.length == 3;
        } catch (Exception e) {
            logger.debug("Invalid JWT format: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsWithoutValidation(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            logger.debug("Error checking token expiration: {}", e.getMessage());
            return true; // Treat as expired if we can't parse
        }
    }
    
    public String getClientNameFromToken(String token) {
        try {
            Claims claims = getClaimsWithoutValidation(token);
            return claims.getSubject();
        } catch (Exception e) {
            logger.debug("Error extracting client Name from token: {}", e.getMessage());
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getScopesFromToken(String token) {
        try {
            Claims claims = getClaimsWithoutValidation(token);
            return (List<String>) claims.get("scopes");
        } catch (Exception e) {
            logger.debug("Error extracting scopes from token: {}", e.getMessage());
            return List.of();
        }
    }
    
    private Claims getClaimsWithoutValidation(String token) {
        // This is for basic parsing without signature validation
        // Signature validation will be done by MCP Server
        /*SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        */
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        return claims;

    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}

   /* public String getSubjectFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        Date expiration = getClaimsFromToken(token).getExpiration();
        return expiration.before(new Date());
    }*/

