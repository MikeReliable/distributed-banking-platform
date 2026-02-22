package com.mike.auth.controller;

import com.mike.auth.config.InternalClientProperties;
import com.mike.auth.security.JwtKeyProvider;
import com.mike.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class JwksController {

    private final JwtKeyProvider keyProvider;
    private final JwtService jwtService;
    private final InternalClientProperties internalClientProperties;

    public JwksController(
            JwtKeyProvider keyProvider,
            JwtService jwtService,
            InternalClientProperties internalClientProperties
    ) {
        this.keyProvider = keyProvider;
        this.jwtService = jwtService;
        this.internalClientProperties = internalClientProperties;
    }

    @GetMapping("/jwks")
    public Map<String, List<Map<String, String>>> jwks() {
        RSAPublicKey publicKey = (RSAPublicKey) keyProvider.getKeyPair().getPublic();

        Map<String, String> key = Map.of(
                "kty", "RSA",
                "kid", "auth-key-1",
                "use", "sig",
                "alg", "RS256",
                "n", base64Url(publicKey.getModulus()),
                "e", base64Url(publicKey.getPublicExponent())
        );

        return Map.of("keys", List.of(key));
    }

    private String base64Url(BigInteger bigInteger) {
        byte[] bytes = bigInteger.toByteArray();

        if (bytes[0] == 0) {
            byte[] tmp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmp, 0, tmp.length);
            bytes = tmp;
        }

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, String>> token(
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            @RequestParam("grant_type") String grantType,
            HttpServletRequest request) {
        if (!"client_credentials".equals(grantType)) {
            return ResponseEntity.badRequest().build();
        }

        if (clientId == null || clientSecret == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Basic ")) {
                String base64Credentials = authHeader.substring(6);
                String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
                String[] parts = credentials.split(":", 2);
                if (parts.length == 2) {
                    clientId = parts[0];
                    clientSecret = parts[1];
                }
            }
        }

        String expectedSecret = internalClientProperties.getInternalClients().get(clientId);
        if (expectedSecret == null || !secretsEqual(clientSecret, expectedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtService.generateServiceToken(clientId);
        return ResponseEntity.ok(Map.of(
                "access_token", token,
                "token_type", "bearer",
                "expires_in", "3600"
        ));
    }

    private boolean secretsEqual(String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8)
        );
    }
}
