package com.mike.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "auth")
public class InternalClientProperties {

    private Map<String, String> internalClients = new HashMap<>();

}
