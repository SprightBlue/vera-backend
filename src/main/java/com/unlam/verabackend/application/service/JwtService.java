package com.unlam.verabackend.application.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private Algorithm getAlgorithm() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Algorithm.HMAC256(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .orElse("");

                
        return JWT.create()
            .withSubject(userDetails.getUsername())
            .withIssuedAt(new Date(System.currentTimeMillis()))
            .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiration))
            .sign(getAlgorithm());
    }

    public String extractUsername(String token) {
        return decodeToken(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = decodeToken(token).getExpiresAt();
        return expiration.before(new Date());
    }

    private DecodedJWT decodeToken(String token) {
        return JWT.require(getAlgorithm())
            .build()
            .verify(token);
    }
}