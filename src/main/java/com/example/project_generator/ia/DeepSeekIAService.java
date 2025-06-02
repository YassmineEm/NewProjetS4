package com.example.project_generator.ia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DeepSeekIAService {

    @Value("${together.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.together.xyz/v1/chat/completions";
    private static final String MODEL_NAME = "mistralai/Mixtral-8x7B-Instruct-v0.1";  // ✅ Disponible

    public String getSecurityAdvice(String projectSummary) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL_NAME);
        body.put("messages", List.of(
            Map.of("role", "system", "content", "Tu es un expert en sécurité logicielle."),
            Map.of("role", "user", "content", "Donne-moi des recommandations de sécurité pour ce projet Spring Boot : " + projectSummary)
        ));
        body.put("temperature", 0.5);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                API_URL, HttpMethod.POST, request, Map.class
            );

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            } else {
                return "⚠️ Aucune réponse générée par le modèle IA.";
            }

        } catch (Exception e) {
            return "❌ Erreur lors de l’appel à Together.ai : " + e.getMessage();
        }
    }
}

