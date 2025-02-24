package com.crewmeister.cmcodingchallenge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.bundesbank")
public class BundesbankConfig {
    private String urlTemplate;
    private List<String> currencies;
}