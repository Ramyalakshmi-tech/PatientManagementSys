package com.pm.authservice.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtUtil {

    private final Key secretKey;
//@Value("${jwt.secret}")
    public JwtUtil(@Value("${jwt.secret}") String secret) {
        try {
            if (!StringUtils.hasText(secret)) { // checks for null, empty, or only whitespace
                throw new IllegalArgumentException("JWT secret must not be empty");
            }

            byte[] keyBytes = Base64.getDecoder()
                    .decode(secret.getBytes(StandardCharsets.UTF_8));
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT secret: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JwtUtil", e);
        }
    }








    public String generateToken(String email, String role) {
        try {
            return Jwts.builder()
                    .subject(email)
                    .claim("role", role)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                    .signWith(secretKey)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT Token", e);
        }
    }

    public void validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (SignatureException e) {
            throw new JwtException("Invalid JWT signature", e);
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT token", e);
        } catch (Exception e) {
            throw new JwtException("Unexpected error during JWT validation", e);
        }
    }
}
