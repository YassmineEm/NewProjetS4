package com.example.project_generator.service;

import com.example.project_generator.configuration.*;
import com.example.project_generator.model.CustomProjectDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.spring.initializr.generator.buildsystem.Dependency;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;


@Service
public class ProjectGenerationService {

    @Value("${project.directory}")
    private Path projectDirectory;

    @Autowired
    private DockerFileContributors dockerFileContributors;

    @Autowired
    private DockerComposeContributor dockerComposeContributor;

    @Autowired
    private ArchitectureContributors architectureContributors;

    @Autowired
    private ProjectSocketContributors projectSocketContributors;

    @Autowired
    private SubconnectManifestContributors subconnectManifestContributors;

    @Autowired
    private CICRPluginInContributors cicrPluginInContributors;

    @Autowired
    private GitLabCIContributor gitLabCIContributor;



    @Autowired
    private Map<String, String> dependencyDescriptions;

    public String generateProject(CustomProjectDescription description) {
        try {
            // 1. Configurer l'architecture
            architectureContributors.configureArchitecture(
            description.getArchitectureType(), 
            projectDirectory,
            description.getGroupId(),
            description.getArtifactId()
        );
        System.out.println("Architecture: " + description.getArchitectureType());
        System.out.println("Artifact: " + description.getArtifactId());
            // 2. Générer le fichier de build approprié
            generateBuildFile(description);


            generateEntities(description);

            // 4. Configurer les sockets
            projectSocketContributors.configureSockets();

            // 5. Générer les fichiers Docker si demandé
            if (description.isGenerateDocker()) {
                dockerFileContributors.setDescription(description);
                dockerFileContributors.contribute(projectDirectory);
                dockerComposeContributor.setDescription(description);
                dockerComposeContributor.contribute(projectDirectory);
            }

            // 6. Générer les manifestes Kubernetes si demandé
            if (description.isGenerateKubernetes()) {
                subconnectManifestContributors.generateKubernetesManifests();
            }

            // 7. Configurer CI/CD si demandé
            if (description.isGenerateCLCG()) {
                cicrPluginInContributors.configureCI();
                gitLabCIContributor.setDescription(description);
                gitLabCIContributor.contribute(projectDirectory);
            }

            return projectDirectory.toAbsolutePath().toString();


        } catch (Exception e) {
            throw new ProjectGenerationException("Failed to generate project: " + e.getMessage(), e);
        }
    }

    private void generateBuildFile(CustomProjectDescription description) throws IOException {
        switch (description.getBuildTool()) {
            case "maven":
                generateMavenPom(description);
                break;
            case "gradle-groovy":
                generateGradleBuildGroovy(description);
                break;
            case "gradle-kotlin":
                generateGradleBuildKotlin(description);
                break;
            default:
                generateMavenPom(description);
        }
    }

    private void generateEntities(CustomProjectDescription description) throws IOException {
        for (String entityName : description.getEntities()) {
            Map<String, Object> model = new HashMap<>();
            model.put("entityName", entityName);
    
            // Convertir le groupId en chemin : ex. "com.example.myapp" -> "com/example/myapp"
            String packagePath = description.getGroupId().replace(".", "/") + "/" + description.getArtifactId().toLowerCase() + "/model";
    
            // Générer le nom de package pour le fichier .java
            String packageName = description.getGroupId() + "." + description.getArtifactId().toLowerCase() + ".model";
            model.put("packageName", packageName);
    
            Path entityPath = projectDirectory.resolve("src/main/java/" + packagePath + "/" + entityName + ".java");
            Files.createDirectories(entityPath.getParent());
    
            // Utilisation de BufferedWriter pour écrire directement le fichier Java
            try (BufferedWriter writer = Files.newBufferedWriter(entityPath)) {
                writer.write("package " + packageName + ";\n\n");
                writer.write("public class " + entityName + " {\n");
                writer.write("    // Define entity fields here\n");
                writer.write("}\n");
            }
        }
    }

    private void generateMavenPom(CustomProjectDescription description) throws IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("groupId", description.getGroupId());
        model.put("artifactId", description.getArtifactId());
        model.put("javaVersion", description.getJavaVersion());
        model.put("dependencies", description.getRequestedDependencies());

        Path pomPath = projectDirectory.resolve("pom.xml");
        generateFromTemplate("pom.xml.ftl", model, pomPath);

        copyResourceToFile("maven-wrapper/mvnw", projectDirectory.resolve("mvnw"));
        copyResourceToFile("maven-wrapper/mvnw.cmd", projectDirectory.resolve("mvnw.cmd"));
        copyResourceToFile("maven-wrapper/.mvn/wrapper/maven-wrapper.jar", projectDirectory.resolve(".mvn/wrapper/maven-wrapper.jar"));
        copyResourceToFile("maven-wrapper/.mvn/wrapper/maven-wrapper.properties", projectDirectory.resolve(".mvn/wrapper/maven-wrapper.properties"));
    }


     private void generateGradleBuildGroovy(CustomProjectDescription description) throws IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("groupId", description.getGroupId());
        model.put("artifactId", description.getArtifactId());
        model.put("javaVersion", description.getJavaVersion());
        model.put("springBootVersion", "3.2.5");
        model.put("dependencies", description.getRequestedDependencies());

        generateFromTemplate("build.gradle.ftl", model, projectDirectory.resolve("build.gradle"));
        generateFromTemplate("settings.gradle.ftl", model, projectDirectory.resolve("settings.gradle"));
    }

    private void generateGradleBuildKotlin(CustomProjectDescription description) throws IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("groupId", description.getGroupId());
        model.put("artifactId", description.getArtifactId());
        model.put("javaVersion", description.getJavaVersion());
        model.put("springBootVersion", "3.2.5");
        model.put("dependencies", description.getRequestedDependencies());

        generateFromTemplate("build.gradle.kts.ftl", model, projectDirectory.resolve("build.gradle.kts"));
        generateFromTemplate("settings.gradle.kts.ftl", model, projectDirectory.resolve("settings.gradle.kts"));
    }

    private void generateFromTemplate(String templateName, Map<String, Object> model, Path outputPath) throws IOException {
        try (Writer writer = Files.newBufferedWriter(outputPath)) {
            freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_32);
            cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates");
            freemarker.template.Template template = cfg.getTemplate(templateName);
            template.process(model, writer);
        } catch (Exception e) {
            throw new IOException("Failed to generate from template: " + templateName, e);
        }
    }

    private void copyResourceToFile(String resourcePath, Path targetPath) throws IOException {
        Files.createDirectories(targetPath.getParent());
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) throw new IOException("Resource not found: " + resourcePath);
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

 

private void addMavenDependencies(Map<String, Dependency> requestedDeps) throws IOException {
    // Ajoutez ici la logique pour ajouter les dépendances Maven dans le fichier pom.xml
    Path pomPath = projectDirectory.resolve("pom.xml");

    // Vous pouvez ici récupérer le contenu existant de pom.xml et y ajouter les nouvelles dépendances
    try (BufferedReader reader = Files.newBufferedReader(pomPath)) {
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append(System.lineSeparator());
        }

        // Ajout des nouvelles dépendances dans la section <dependencies>
        for (Map.Entry<String, Dependency> entry : requestedDeps.entrySet()) {
            Dependency dep = entry.getValue();
            content.append("<dependency>").append(System.lineSeparator())
                   .append("    <groupId>").append(dep.getGroupId()).append("</groupId>").append(System.lineSeparator())
                   .append("    <artifactId>").append(dep.getArtifactId()).append("</artifactId>").append(System.lineSeparator())
                   .append("    <version>").append(dep.getVersion()).append("</version>").append(System.lineSeparator())
                   .append("</dependency>").append(System.lineSeparator());
        }

        // Écrire le contenu mis à jour dans le fichier
        try (BufferedWriter writer = Files.newBufferedWriter(pomPath)) {
            writer.write(content.toString());
        }
    } catch (IOException e) {
        throw new IOException("Failed to update pom.xml with dependencies", e);
    }
}

private void addGradleDependencies(Map<String, Dependency> requestedDeps, String buildTool) throws IOException {
    // Ajout des dépendances dans build.gradle
    Path gradlePath = projectDirectory.resolve(buildTool.equals("gradle-groovy") ? "build.gradle" : "build.gradle.kts");

    StringBuilder content = new StringBuilder();
    for (Map.Entry<String, Dependency> entry : requestedDeps.entrySet()) {
        Dependency dep = entry.getValue();
        if (buildTool.equals("gradle-groovy")) {
            content.append("implementation '").append(dep.getGroupId()).append(":").append(dep.getArtifactId()).append(":").append(dep.getVersion()).append("'").append(System.lineSeparator());
        } else {
            content.append("implementation(\"").append(dep.getGroupId()).append(":").append(dep.getArtifactId()).append(":").append(dep.getVersion()).append("\")").append(System.lineSeparator());
        }
    }

    // Ajouter ces dépendances dans le fichier build.gradle (groovy ou kotlin)
    try (BufferedReader reader = Files.newBufferedReader(gradlePath)) {
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append(System.lineSeparator());
        }

        // Écrire le contenu mis à jour dans le fichier
        try (BufferedWriter writer = Files.newBufferedWriter(gradlePath)) {
            writer.write(content.toString());
        }
    } catch (IOException e) {
        throw new IOException("Failed to update build.gradle with dependencies", e);
    }
}


    public static class ProjectGenerationException extends RuntimeException {
        public ProjectGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
