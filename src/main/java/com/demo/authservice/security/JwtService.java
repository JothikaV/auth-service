package com.demo.authservice.security;


import com.demo.authservice.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Service for generating, validating, and extracting information from JWT tokens.
 *
 * <p>This service uses a secret key (configured via {@code auth.jwt.secret}) to
 * sign and validate tokens using the HS256 algorithm. Tokens include custom claims
 * such as username, email, and roles, and have a fixed expiration of 24 hours.</p>
 *
 * <p>Main responsibilities:</p>
 * <ul>
 *   <li>Generate JWT tokens for authenticated users.</li>
 *   <li>Extract the username/email from a given token.</li>
 *   <li>Validate token authenticity and expiration.</li>
 * </ul>
 *
 * <p>Tokens are typically used in HTTP Authorization headers to secure endpoints
 * in combination with a JWT filter.</p>
 */


@Service
public class JwtService {

    @Value("${auth.jwt.secret}")
    private String secret;

    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 hours

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Generate JWT token with email as subject and roles as claim
    public String generateToken(UserEntity userEntity) {
        return Jwts.builder()
                .setSubject(userEntity.getEmail())
                .claim("username", userEntity.getUsername())  // custom claim
                .claim("email", userEntity.getEmail())
                .claim("roles", userEntity.getRoles()
                        .stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toList()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract email from token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Validate token
    public boolean isTokenValid(String token, UserEntity userEntity) {
        String email = extractUsername(token);
        return (email.equals(userEntity.getEmail()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

