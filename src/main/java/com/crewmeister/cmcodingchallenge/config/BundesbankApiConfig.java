package com.crewmeister.cmcodingchallenge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "bundesbank.api")
public class BundesbankApiConfig {
    private Map<String, String> urls = new HashMap<>();
}