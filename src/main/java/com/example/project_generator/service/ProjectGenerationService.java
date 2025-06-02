package com.example.project_generator.service;

import com.example.project_generator.configuration.*;
import com.example.project_generator.model.CustomProjectDescription;
import com.example.project_generator.model.FieldDefinition;

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
import java.util.List;
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
            generateRestControllers(description);
           
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
      Map<String, List<FieldDefinition>> entityFieldsMap = description.getEntityFields();

      for (String entityName : description.getEntities()) {
        List<FieldDefinition> fields = entityFieldsMap.get(entityName);
        if (fields == null) continue;

        String packageName = description.getGroupId() + "." + description.getArtifactId().toLowerCase() + ".model";
        String packagePath = packageName.replace(".", "/");
        Path entityPath = projectDirectory.resolve("src/main/java/" + packagePath + "/" + entityName + ".java");
        Files.createDirectories(entityPath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(entityPath)) {
            writer.write("package " + packageName + ";\n\n");
            writer.write("import jakarta.persistence.*;\n");
            writer.write("import lombok.*;\n");
            writer.write("import java.time.*;\n\n");

            writer.write("@Entity\n@Data\n@NoArgsConstructor\n@AllArgsConstructor\n@Builder\n");
            writer.write("public class " + entityName + " {\n\n");

            for (FieldDefinition field : fields) {
                if (field.isPrimaryKey()) {
                    writer.write("    @Id\n");
                    writer.write("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
                }

                if (field.isNotNull()) {
                    writer.write("    @Column(nullable = false)\n");
                }

                writer.write("    private " + field.getType() + " " + field.getName() + ";\n\n");
            }

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
    }

    private void generateRestControllers(CustomProjectDescription description) throws IOException {
       Map<String, List<FieldDefinition>> fieldsMap = description.getEntityFields();
       String architecture = description.getArchitectureType();
       String groupId = description.getGroupId();
       String artifactId = description.getArtifactId().toLowerCase();

       String basePackage = groupId.replace(".", "/") + "/" + artifactId;

       for (String entity : description.getEntities()) {
          if (Boolean.TRUE.equals(description.getRestEndpoints().get(entity))) {

            List<FieldDefinition> fields = fieldsMap.get(entity);
            FieldDefinition primaryKey = null;

            if (fields != null) {
                for (FieldDefinition field : fields) {
                    if (field.isPrimaryKey()) {
                        primaryKey = field;
                        break;
                    }
                }
            }

            if (primaryKey == null) {
                throw new IllegalStateException("Aucune clé primaire définie pour l'entité " + entity);
            }

            String idType = primaryKey.getType();
            String idName = primaryKey.getName();

            String controllerPackagePath;
            String controllerPackageName;
            String modelPackageName;

            if ("hexagonale".equalsIgnoreCase(architecture)) {
                controllerPackagePath = basePackage + "/infrastructure/rest";
                controllerPackageName = groupId + "." + artifactId + ".infrastructure.rest";
                modelPackageName = groupId + "." + artifactId + ".domain.model";
            } else {
                controllerPackagePath = basePackage + "/controller";
                controllerPackageName = groupId + "." + artifactId + ".controller";
                modelPackageName = groupId + "." + artifactId + ".model";
            }

            String controllerClassName = entity + "Controller";
            Path controllerPath = projectDirectory.resolve("src/main/java/" + controllerPackagePath + "/" + controllerClassName + ".java");
            Files.createDirectories(controllerPath.getParent());

            String content = "package " + controllerPackageName + ";\n\n" +
               "import org.springframework.web.bind.annotation.*;\n" +
               "import " + modelPackageName + "." + entity + ";\n" +
               "import java.util.*;\n\n" +
               "@RestController\n" +
               "@RequestMapping(\"/" + entity.toLowerCase() + "s\")\n" +
               "public class " + controllerClassName + " {\n\n" +
               "    @GetMapping\n" +
               "    public List<" + entity + "> getAll() {\n" +
               "        return new ArrayList<>();\n" +
               "    }\n\n" +
               "    @PostMapping\n" +
               "    public " + entity + " create(@RequestBody " + entity + " obj) {\n" +
               "        return obj;\n" +
               "    }\n\n" +
               "    @GetMapping(\"/{" + idName + "}\")\n" +
               "    public " + entity + " getById(@PathVariable " + idType + " " + idName + ") {\n" +
               "        return new " + entity + "();\n" +
               "    }\n\n" +
               "    @PutMapping(\"/{" + idName + "}\")\n" +
               "    public " + entity + " update(@PathVariable " + idType + " " + idName + ", @RequestBody " + entity + " obj) {\n" +
               "        obj.set" + capitalize(idName) + "(" + idName + ");\n" +
               "        return obj;\n" +
               "    }\n\n" +
               "    @DeleteMapping(\"/{" + idName + "}\")\n" +
               "    public void delete(@PathVariable " + idType + " " + idName + ") {\n" +
               "    }\n" +
               "}\n";

            Files.write(controllerPath, content.getBytes());
        }
    }
}


    public static class ProjectGenerationException extends RuntimeException {
        public ProjectGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
