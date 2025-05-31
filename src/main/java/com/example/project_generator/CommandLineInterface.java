package com.example.project_generator;

import com.example.project_generator.controller.ProjectGeneratorController;
import com.example.project_generator.model.CustomProjectRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
public class CommandLineInterface implements CommandLineRunner {

    private final ProjectGeneratorController projectGeneratorController;

    @Autowired
    public CommandLineInterface(ProjectGeneratorController projectGeneratorController) {
        this.projectGeneratorController = projectGeneratorController;
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        CustomProjectRequest request = new CustomProjectRequest();

        System.out.println("=== Spring Project Generator ===");
        
        
        System.out.print("Nom du projet: ");
        request.setName(scanner.nextLine());

        System.out.print("Group  (défaut: com.example): ");
        String groupId = scanner.nextLine();
        request.setGroupId(groupId.isEmpty() ? "com.example" : groupId);
        
        System.out.print("Artifact : ");
        String artifactId = scanner.nextLine().trim(); 
        if (artifactId.isEmpty()) {
           System.err.println("Erreur : L'Artifact ID ne peut pas être vide");
           return;
        }
        request.setArtifactId(artifactId);
        
        System.out.print("Version Java (défaut: 17): ");
        String javaVersion = scanner.nextLine();
        request.setJavaVersion(javaVersion.isEmpty() ? "17" : javaVersion);

        
       System.out.print("Version Spring Boot (défaut: 3.4.4): ");
       String springBootVersion = scanner.nextLine();
       request.setSpringBootVersion(springBootVersion.isEmpty() ? "3.4.4" : springBootVersion);

        System.out.print("Build tool (1. Maven, 2. Gradle Groovy, 3. Gradle Kotlin) - défaut 1: ");
        String buildToolChoice = scanner.nextLine();
        request.setBuildTool(
            switch (buildToolChoice) {
             case "2" -> "gradle-groovy";
             case "3" -> "gradle-kotlin";
             default -> "maven";
            });

        
        System.out.print("Port (défaut: 8080): ");
        String portInput = scanner.nextLine();
        request.setPort(portInput.isEmpty() ? 8080 : Integer.parseInt(portInput));
        
        System.out.print("Profile (défaut: dev): ");
        String profile = scanner.nextLine();
        request.setProfile(profile.isEmpty() ? "dev" : profile);

     
        System.out.print("Générer Docker ? (y/n): ");
        request.setGenerateDocker(scanner.nextLine().equalsIgnoreCase("y"));
        
        if (request.isGenerateDocker()) {
            System.out.print("Docker repository (défaut: your-default-repo): ");
            String dockerRepo = scanner.nextLine();
            request.setDockerRepository(dockerRepo.isEmpty() ? "your-default-repo" : dockerRepo);
        }
        
        System.out.print("Générer Kubernetes ? (y/n): ");
        request.setGenerateKubernetes(scanner.nextLine().equalsIgnoreCase("y"));
        
        System.out.print("Générer CI/CD (GitLab CI) ? (y/n): ");
        request.setGenerateCLCG(scanner.nextLine().equalsIgnoreCase("y"));

        System.out.println("Type d'architecture (choisissez un numéro):");
        System.out.println("1. Hexagonale");
        System.out.println("2. En couches");
        System.out.print("Votre choix: ");
        int archChoice = Integer.parseInt(scanner.nextLine());
        request.setArchitectureType(switch(archChoice) {
            case 1 -> "hexagonale";
            case 2 -> "en-couches";
            default -> "standard";
        });

        
        System.out.println("Dépendances disponibles (entrez les numéros séparés par des virgules):");
        System.out.println("1. Web (Spring Web)");
        System.out.println("2. JPA (Spring Data JPA)");
        System.out.print("Votre choix: ");
        String depChoice = scanner.nextLine();

        Set<String> dependencies = new HashSet<>();
        if (!depChoice.isEmpty()) {
        for (String num : depChoice.split(",")) {
          switch (num.trim()) {
            case "1": dependencies.add("web"); break;
            case "2": dependencies.add("data-jpa"); break;
          }
       }
    }
request.setDependencies(dependencies);

        
        List<String> entities = new ArrayList<>();
        System.out.println("Entrez les noms des entités (une par ligne, vide pour terminer):");
        while (true) {
            System.out.print("Entité: ");
            String entity = scanner.nextLine();
            if (entity.isEmpty()) break;
            entities.add(entity);
        }
        request.setEntities(entities);

        
        System.out.println("\nGénération du projet en cours...");
        try {
            byte[] zipBytes = projectGeneratorController.generateProject(request).getBody();
            String fileName = request.getArtifactId() + ".zip";
            java.nio.file.Files.write(java.nio.file.Path.of(fileName), zipBytes);
            System.out.println("Projet généré avec succès dans le fichier: " + fileName);
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du projet: " + e.getMessage());
        }
        
        scanner.close();
    }
}