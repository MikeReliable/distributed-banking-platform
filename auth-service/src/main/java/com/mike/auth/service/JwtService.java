package com.mike.auth.service;

import com.mike.auth.domain.Role;
import com.mike.auth.security.JwtKeyProvider;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private static final long EXPIRATION_MS = 86400000;
    private static final String KEY_ID = "auth-key-1";

    private final JwtKeyProvider keyProvider;

    public JwtService(JwtKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public String generateToken(String userId, Role role) {
        return Jwts.builder()
                .setHeaderParam("kid", KEY_ID)
                .setSubject(userId)
                .claim("role", role.name())
                .setIssuer("auth-service")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(keyProvider.getKeyPair().getPrivate())
                .compact();
    }

    public String generateServiceToken(String clientId) {
        return Jwts.builder()
                .setHeaderParam("kid", KEY_ID)
                .setSubject(clientId)
                .claim("role", Role.SERVICE.name())
                .setIssuer("auth-service")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(keyProvider.getKeyPair().getPrivate())
                .compact();
    }
}
