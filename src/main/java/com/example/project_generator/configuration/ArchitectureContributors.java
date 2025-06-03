package com.example.project_generator.configuration;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.project_generator.model.CustomProjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ArchitectureContributors {


    public void configureArchitecture(String architectureType, Path projectRoot, String groupId , String artifactId) throws IOException {
        System.out.println("Configuring architecture: " + architectureType);

        switch (architectureType.toLowerCase()) {
            case "en-couches" -> generateLayeredArchitecture(projectRoot, groupId, artifactId);
            case "hexagonale" -> generateHexagonalArchitecture(projectRoot, groupId, artifactId);
            default -> generateDefaultArchitecture(projectRoot);
        }
    }

    private void generateLayeredArchitecture(Path projectRoot, String groupId, String artifactId) throws IOException {
        
        String basePackage = groupId.replace(".", "/") + "/" + artifactId.toLowerCase();
        Path mainJavaPath = projectRoot.resolve("src/main/java/" + basePackage);
    
       
        createDirectories(mainJavaPath, "config", "controller", "model", "repository", "service");
    
        
        String basePackageName = groupId + "." + artifactId.toLowerCase();
    }
    
    private void generateHexagonalArchitecture(Path projectRoot, String groupId, String artifactId) throws IOException {
        String packageName = groupId + "." + artifactId.toLowerCase();
        String packagePath = packageName.replace(".", "/");
        Path basePath = projectRoot.resolve("src/main/java/" + packagePath);
        createDirectories(basePath,
            "domain/model",   
            "domain/port/in",      
            "domain/port/out",      
            "application/service",  
            "infrastructure/rest",  
            "infrastructure/persistence", 
            "infrastructure/config"              
        );
    }


    private void generateDefaultArchitecture(Path projectRoot) throws IOException {
        
        System.out.println("No valid architecture selected, skipping structure generation.");
    }

    private void generateBaseClass(Path basePath, String packageName, String className, String fullPackageName) throws IOException {
        Path packagePath = basePath.resolve(packageName);
        Files.createDirectories(packagePath);
    
        Path filePath = packagePath.resolve(className + ".java");
        if (!Files.exists(filePath)) {
            String content = "package " + fullPackageName + "." + packageName + ";\n\n" +  
                             "public class " + className + " {\n" +
                             "    // TODO: Implement " + className + " functionality\n" +
                             "}\n";
            Files.write(filePath, content.getBytes());
        }
    }

    private void createDirectories(Path basePath, String... subDirs) throws IOException {
      for (String dir : subDirs) {
        Path fullPath = basePath.resolve(dir);
        Files.createDirectories(fullPath);

        // Ajouter un fichier temporaire vide pour forcer l'inclusion dans le ZIP
        Path placeholder = fullPath.resolve(".gitkeep");
        Files.writeString(placeholder, "");  // fichier vide
      }
    }
}

