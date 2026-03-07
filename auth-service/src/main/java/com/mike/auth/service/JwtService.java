package com.mike.auth.service;

import com.mike.auth.domain.Role;
import com.mike.auth.security.JwtKeyProvider;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private static final long EXPIRATION_MS = 86400000;
    private static final long EXPIRATION_SECONDS = EXPIRATION_MS / 1000;
    private static final String KEY_ID = "auth-key-1";

    private final JwtKeyProvider keyProvider;
    private final String issuer;
    private final String userAudience;
    private final String serviceAudience;

    public JwtService(
            JwtKeyProvider keyProvider,
            @Value("${auth.jwt.issuer:auth-service}") String issuer,
            @Value("${auth.jwt.audience.user:bank-rest-api}") String userAudience,
            @Value("${auth.jwt.audience.service:bank-rest-internal}") String serviceAudience
    ) {
        this.keyProvider = keyProvider;
        this.issuer = issuer;
        this.userAudience = userAudience;
        this.serviceAudience = serviceAudience;
    }

    public String generateToken(String userId, Role role) {
        return Jwts.builder()
                .setHeaderParam("kid", KEY_ID)
                .setSubject(userId)
                .claim("role", role.name())
                .claim("aud", List.of(userAudience))
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(keyProvider.getKeyPair().getPrivate())
                .compact();
    }

    public String generateServiceToken(String clientId) {
        return Jwts.builder()
                .setHeaderParam("kid", KEY_ID)
                .setSubject(clientId)
                .claim("role", Role.ROLE_SERVICE.name())
                .claim("aud", List.of(serviceAudience))
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(keyProvider.getKeyPair().getPrivate())
                .compact();
    }

    public long getExpirationSeconds() {
        return EXPIRATION_SECONDS;
    }
}
