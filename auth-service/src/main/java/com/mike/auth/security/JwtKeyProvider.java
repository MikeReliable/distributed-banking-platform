package com.mike.auth.security;

import com.mike.auth.config.JwtKeystoreProperties;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

@Getter
@Component
public class JwtKeyProvider {

    private final KeyPair keyPair;

    public JwtKeyProvider(JwtKeystoreProperties properties) {
        try {
            KeyStore keyStore = KeyStore.getInstance(properties.getType());
            Path keyStorePath = Path.of(properties.getPath());

            try (InputStream inputStream = Files.newInputStream(keyStorePath)) {
                keyStore.load(inputStream, properties.getPassword().toCharArray());
            }

            Key key = keyStore.getKey(properties.getAlias(), properties.getPassword().toCharArray());
            if (!(key instanceof PrivateKey privateKey)) {
                throw new IllegalStateException("JWT private key not found in keystore");
            }

            Certificate certificate = keyStore.getCertificate(properties.getAlias());
            if (certificate == null) {
                throw new IllegalStateException("JWT certificate not found in keystore");
            }

            PublicKey publicKey = certificate.getPublicKey();
            this.keyPair = new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
