package com.example.project_generator.ia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class TestIaGeneratorService {

    @Value("${together.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.together.xyz/v1/chat/completions";
    private static final String MODEL_NAME = "deepseek-coder-33b-instruct";


    public String generateTestClass(String className, String classCode) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL_NAME);
        body.put("messages", List.of(
           Map.of("role", "system", "content", "You are a Java code generator. Generate only clean Java code, with no explanations or comments. Use JUnit 5 and Mockito. Do not use @SpringBootTest or database. Always return only Java code."),
           Map.of("role", "user", "content", "Generate a unit test for the following Java class:\n\n" + classCode)
        ));

        body.put("temperature", 0.3);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(API_URL, HttpMethod.POST, request, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            return "// ❌ Erreur lors de l’appel IA : " + e.getMessage();
        }

        return "// ⚠️ Aucun test généré.";
    }
}
