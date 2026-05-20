package com.pizza.delivery.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.refresh-secret}")
    private String jwtRefreshSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public String generateAccessToken(String userId, String email, String role) {
        return buildToken(userId, email, role, jwtExpirationMs, getSigningKey(jwtSecret));
    }

    public String generateRefreshToken(String userId, String email, String role) {
        return buildToken(userId, email, role, refreshExpirationMs, getSigningKey(jwtRefreshSecret));
    }

    private String buildToken(String userId, String email, String role, long expirationMs, SecretKey key) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId)
                .claims(Map.of("email", email, "role", role))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return getClaims(token, getSigningKey(jwtSecret)).getSubject();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token, getSigningKey(jwtSecret)).get("email", String.class);
    }

    public String getRoleFromToken(String token) {
        return getClaims(token, getSigningKey(jwtSecret)).get("role", String.class);
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, getSigningKey(jwtSecret));
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, getSigningKey(jwtRefreshSecret));
    }

    public String getUserIdFromRefreshToken(String token) {
        return getClaims(token, getSigningKey(jwtRefreshSecret)).getSubject();
    }

    public long getAccessTokenExpirationMs() {
        return jwtExpirationMs;
    }

    private Claims getClaims(String token, SecretKey key) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private boolean validateToken(String token, SecretKey key) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private SecretKey getSigningKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
