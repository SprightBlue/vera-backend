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

    // Construye el algoritmo de firma a partir de la clave secreta en Base64.
    private Algorithm getAlgorithm() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Algorithm.HMAC256(keyBytes);
    }

    // Genera el JWT con el email como subject.
    public String generateToken(UserDetails userDetails) {
        return JWT.create()
            .withSubject(userDetails.getUsername())
            .withIssuedAt(new Date(System.currentTimeMillis()))
            .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiration))
            .sign(getAlgorithm());
    }

    // Extrae el email (subject) del token.
    public String extractUsername(String token) {
        return decodeToken(token).getSubject();
    }

    // Valida que el token pertenezca al usuario y no haya expirado.
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JWTVerificationException e) {
            // Si el token está mal firmado o expiró, Auth0 lanza excepción.
            // Lo capturamos y devolvemos false en lugar de propagar el error.
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = decodeToken(token).getExpiresAt();
        return expiration.before(new Date());
    }

    // Método central: verifica la firma y decodifica el token.
    // Si la firma es inválida, lanza JWTVerificationException automáticamente.
    private DecodedJWT decodeToken(String token) {
        return JWT.require(getAlgorithm())
            .build()
            .verify(token);
    }
}