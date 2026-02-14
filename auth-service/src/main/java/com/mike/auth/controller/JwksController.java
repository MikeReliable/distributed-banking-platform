package com.mike.auth.controller;

import com.mike.auth.security.JwtKeyProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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
}
