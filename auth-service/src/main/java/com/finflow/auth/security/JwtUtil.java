package com.finflow.auth.security;

import com.finflow.auth.model.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // 🔐 Generate signing key
    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 🔥 GENERATE TOKEN (USING ENUM)
    public String generateToken(String email, Role role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name()) // ✅ store enum as string
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 📌 Extract email
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // 📌 Extract role as ENUM
    public Role extractRole(String token) {
        String role = getClaims(token).get("role", String.class);
        return Role.valueOf(role); // ✅ convert back to enum
    }

    // 📌 Validate token
    public boolean validateToken(String token) {
        return !getClaims(token).getExpiration().before(new Date());
    }

    // 🔍 Get all claims
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}