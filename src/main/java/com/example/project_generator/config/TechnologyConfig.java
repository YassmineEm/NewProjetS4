package com.example.project_generator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
public class TechnologyConfig {

    @Bean
    public Map<String, Set<String>> compatibleVersions() {
        Map<String, Set<String>> versions = new HashMap<>();
        
        // Versions compatibles Java - Spring Boot
        versions.put("java17", Set.of("3.4.4", "3.3.11", "3.2.0"));
        versions.put("java21", Set.of("3.5.0", "3.4.6"));
        
        // Versions compatibles Build Tool - Language
        versions.put("maven-java", Set.of("3.9.9", "3.8.6"));
        versions.put("gradle-groovy", Set.of("8.4", "8.3"));
        versions.put("gradle-kotlin", Set.of("8.4", "8.3"));
        
        return versions;
    }

    @Bean
    public Map<String, String> dependencyDescriptions() {
        Map<String, String> deps = new HashMap<>();
        deps.put("web", "Spring Web MVC");
        deps.put("data-jpa", "Spring Data JPA");
        deps.put("security", "Spring Security");
        deps.put("lombok", "Project Lombok");
        // Ajouter d'autres dépendances courantes...
        return deps;
    }
}