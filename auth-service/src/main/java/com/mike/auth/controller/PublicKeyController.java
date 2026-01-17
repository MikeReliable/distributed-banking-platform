package com.mike.auth.controller;

import com.mike.auth.security.JwtKeyProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@RestController
@RequestMapping("/auth")
public class PublicKeyController {

    private final JwtKeyProvider keyProvider;

    public PublicKeyController(JwtKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @GetMapping("/public-key")
    public String publicKey() {
        RSAPublicKey key = (RSAPublicKey) keyProvider.getKeyPair().getPublic();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
