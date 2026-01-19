package com.mike.auth.controller;

import com.mike.auth.security.JwtKeyProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class JwksController {

    private final JwtKeyProvider keyProvider;

    public JwksController(JwtKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @GetMapping("/jwks")
    public Map<String, List<Map<String, String>>> jwks() {
        RSAPublicKey publicKey = (RSAPublicKey) keyProvider.getKeyPair().getPublic();

        Map<String, String> key = Map.of(
                "kty", "RSA",
                "kid", "auth-key-1",
                "use", "sig",
                "alg", "RS256",
                "n", java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray()),
                "e", java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray())
        );

        return Map.of("keys", List.of(key));
    }
}
