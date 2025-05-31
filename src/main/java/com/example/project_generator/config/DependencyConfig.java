package com.example.project_generator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DependencyConfig {

    @Bean
    public Map<String, String> dependencyDescriptions() {
        Map<String, String> map = new HashMap<>();
        map.put("web", "spring-boot-starter-web");
        map.put("jpa", "spring-boot-starter-data-jpa");
        map.put("security", "spring-boot-starter-security");

        return map;
    }
}

