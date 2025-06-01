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
import java.util.HashSet;
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
           
            architectureContributors.configureArchitecture(
            description.getArchitectureType(), 
            projectDirectory,
            description.getGroupId(),
            description.getArtifactId()
        );
        generateMainApplication(description);

        System.out.println("Architecture: " + description.getArchitectureType());
        System.out.println("Artifact: " + description.getArtifactId());
           
            generateBuildFile(description);
            generateApplicationProperties(description);

            generateEntities(description);

           
            projectSocketContributors.configureSockets();

            
            if (description.isGenerateDocker()) {
                dockerFileContributors.setDescription(description);
                dockerFileContributors.contribute(projectDirectory);
                dockerComposeContributor.setDescription(description);
                dockerComposeContributor.contribute(projectDirectory);
            }

           
            if (description.isGenerateKubernetes()) {
                subconnectManifestContributors.generateKubernetesManifests();
            }

           
            if (description.isGenerateCLCG()) {
                cicrPluginInContributors.configureCI();
                gitLabCIContributor.setDescription(description);
                gitLabCIContributor.contribute(projectDirectory);
            }

            generateTestClass(description);
            generateGitFiles();
            generateDocumentation(description);

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
    
            
            String packagePath = description.getGroupId().replace(".", "/") + "/" + description.getArtifactId().toLowerCase() + "/model";
    
            
            String packageName = description.getGroupId() + "." + description.getArtifactId().toLowerCase() + ".model";
            model.put("packageName", packageName);
    
            Path entityPath = projectDirectory.resolve("src/main/java/" + packagePath + "/" + entityName + ".java");
            Files.createDirectories(entityPath.getParent());
    
            
            try (BufferedWriter writer = Files.newBufferedWriter(entityPath)) {
                writer.write("package " + packageName + ";\n\n");
                writer.write("public class " + entityName + " {\n");
                writer.write("    // Define entity fields here\n");
                writer.write("}\n");
            }
        }
    }


    private void generateMainApplication(CustomProjectDescription description) throws IOException {
        String className = capitalize(description.getArtifactId()) + "Application";
        String packagePath = description.getGroupId().replace(".", "/") + "/" + description.getArtifactId().toLowerCase();
        String packageName = description.getGroupId() + "." + description.getArtifactId().toLowerCase();

        Path mainPath = projectDirectory.resolve("src/main/java/" + packagePath);
        Files.createDirectories(mainPath);

        Path appFile = mainPath.resolve(className + ".java");
        if (!Files.exists(appFile)) {
          String content = "package " + packageName + ";\n\n"
            + "import org.springframework.boot.SpringApplication;\n"
            + "import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n"
            + "@SpringBootApplication\n"
            + "public class " + className + " {\n"
            + "    public static void main(String[] args) {\n"
            + "        SpringApplication.run(" + className + ".class, args);\n"
            + "    }\n"
            + "}\n";
          Files.write(appFile, content.getBytes());
        }
    }


    private void generateMavenPom(CustomProjectDescription description) throws IOException {
        if (description.getDependencies() == null) {
           description.setDependencies(new HashSet<>());
        }

        Map<String, Object> model = new HashMap<>();
        model.put("description", description);
        model.put("mavenVersion", description.getMavenVersion());


        Path pomPath = projectDirectory.resolve("pom.xml");
        generateFromTemplate("pom.xml.ftl", model, pomPath);

        copyResourceToFile("maven-wrapper/mvnw", projectDirectory.resolve("mvnw"));
        copyResourceToFile("maven-wrapper/mvnw.cmd", projectDirectory.resolve("mvnw.cmd"));
        copyResourceToFile("maven-wrapper/.mvn/wrapper/maven-wrapper.jar", projectDirectory.resolve(".mvn/wrapper/maven-wrapper.jar"));
        generateFromTemplate("maven-wrapper.properties.ftl", model, projectDirectory.resolve(".mvn/wrapper/maven-wrapper.properties"));

        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
           projectDirectory.resolve("mvnw").toFile().setExecutable(true);
        }
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


    private void generateTestClass(CustomProjectDescription description) throws IOException {
      String basePackagePath = description.getGroupId().replace(".", "/") + "/" + description.getArtifactId().toLowerCase();
      String packageName = description.getGroupId() + "." + description.getArtifactId().toLowerCase();
      String className = capitalize(description.getArtifactId()) + "ApplicationTests";

      Path testPath = projectDirectory.resolve("src/test/java/" + basePackagePath);
      Files.createDirectories(testPath);

      Path testFile = testPath.resolve(className + ".java");

      String content = "package " + packageName + ";\n\n"
        + "import org.junit.jupiter.api.Test;\n"
        + "import org.springframework.boot.test.context.SpringBootTest;\n\n"
        + "@SpringBootTest\n"
        + "public class " + className + " {\n\n"
        + "    @Test\n"
        + "    void contextLoads() {\n"
        + "    }\n"
        + "}\n";

      Files.write(testFile, content.getBytes());
    }


    private void generateApplicationProperties(CustomProjectDescription description) throws IOException {
        Path resourcesPath = projectDirectory.resolve("src/main/resources");
        Files.createDirectories(resourcesPath);

        Path propertiesFile = resourcesPath.resolve("application.properties");

        String content = "server.port=" + description.getPort() + "\n"
                   + "spring.profiles.active=" + description.getProfile() + "\n";

        Files.write(propertiesFile, content.getBytes());
    }


    private void generateGitFiles() throws IOException {
        Map<String, Object> model = new HashMap<>(); 

        generateFromTemplate("gitignore.ftl", model, projectDirectory.resolve(".gitignore"));
        generateFromTemplate("gitattributes.ftl", model, projectDirectory.resolve(".gitattributes"));
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
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
        if (in == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        Files.createDirectories(targetPath.getParent());
        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
}


private String capitalize(String str) {
    if (str == null || str.isEmpty()) return str;
    return str.substring(0, 1).toUpperCase() + str.substring(1);
}


 

private void addMavenDependencies(Map<String, Dependency> requestedDeps) throws IOException {
    
    Path pomPath = projectDirectory.resolve("pom.xml");

    
    try (BufferedReader reader = Files.newBufferedReader(pomPath)) {
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append(System.lineSeparator());
        }

        
        for (Map.Entry<String, Dependency> entry : requestedDeps.entrySet()) {
            Dependency dep = entry.getValue();
            content.append("<dependency>").append(System.lineSeparator())
                   .append("    <groupId>").append(dep.getGroupId()).append("</groupId>").append(System.lineSeparator())
                   .append("    <artifactId>").append(dep.getArtifactId()).append("</artifactId>").append(System.lineSeparator())
                   .append("    <version>").append(dep.getVersion()).append("</version>").append(System.lineSeparator())
                   .append("</dependency>").append(System.lineSeparator());
        }

        
        try (BufferedWriter writer = Files.newBufferedWriter(pomPath)) {
            writer.write(content.toString());
        }
    } catch (IOException e) {
        throw new IOException("Failed to update pom.xml with dependencies", e);
    }
}

private void addGradleDependencies(Map<String, Dependency> requestedDeps, String buildTool) throws IOException {
    
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

    
    try (BufferedReader reader = Files.newBufferedReader(gradlePath)) {
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append(System.lineSeparator());
        }

    
        try (BufferedWriter writer = Files.newBufferedWriter(gradlePath)) {
            writer.write(content.toString());
        }
    } catch (IOException e) {
        throw new IOException("Failed to update build.gradle with dependencies", e);
    }
}

    private void generateDocumentation(CustomProjectDescription description) throws IOException {
    // === Génération du README.md ===
        Map<String, Object> readmeModel = new HashMap<>();
        readmeModel.put("name", description.getName());
        readmeModel.put("artifactId", description.getArtifactId());
        readmeModel.put("description", "Project generated by Spring Project Generator");
        readmeModel.put("javaVersion", description.getJavaVersion());
        readmeModel.put("springBootVersion", description.getSpringBootVersion());
        readmeModel.put("buildTool", description.getBuildTool());
        readmeModel.put("architectureType", description.getArchitectureType());
        readmeModel.put("port", description.getPort());
        readmeModel.put("groupId", description.getGroupId());

        generateFromTemplate("Readme.md.ftl", readmeModel, projectDirectory.resolve("README.md"));

    // === Génération du SwaggerConfig.java si web est présent ===
        if (description.getDependencies() != null && description.getDependencies().contains("web")) {
          String packagePath = description.getGroupId().replace(".", "/") + "/" + description.getArtifactId().toLowerCase() + "/config";
          Path configPath = projectDirectory.resolve("src/main/java/" + packagePath);
          Files.createDirectories(configPath);

          Map<String, Object> swaggerModel = new HashMap<>();
          swaggerModel.put("packageName", description.getGroupId() + "." + description.getArtifactId().toLowerCase());
          swaggerModel.put("name", description.getName());
          swaggerModel.put("artifactId", description.getArtifactId());
          swaggerModel.put("version", description.getVersion());
          swaggerModel.put("description", "API documentation");
          swaggerModel.put("license", "MIT");

          generateFromTemplate("swagger-config.ftl", swaggerModel, configPath.resolve("SwaggerConfig.java"));
    }
}

   


    public static class ProjectGenerationException extends RuntimeException {
        public ProjectGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
