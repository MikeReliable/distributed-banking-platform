package com.mike.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth.jwt.keystore")
public class JwtKeystoreProperties {

    private String path;
    private String password;
    private String alias;
    private String type = "PKCS12";
}
