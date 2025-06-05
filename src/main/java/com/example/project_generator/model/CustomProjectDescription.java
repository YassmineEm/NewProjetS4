package com.example.project_generator.model;


import io.spring.initializr.generator.language.Language;
import io.spring.initializr.generator.language.java.JavaLanguage;
import io.spring.initializr.generator.packaging.Packaging;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.version.Version;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.buildsystem.BuildSystem;
import io.spring.initializr.generator.buildsystem.Dependency;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CustomProjectDescription implements ProjectDescription {

    private String architectureType;
    private boolean generateDocker;
    private boolean generateKubernetes;
    private boolean generateCLCG;
    private List<String> entities;
    private String mavenVersion;

    private Language language = new JavaLanguage(); 
    private String groupId = "com.example";
    private String artifactId;
    private String name;
    private String version = "0.0.1-SNAPSHOT";
    private String packageName;


    private String javaVersion = "17";
    private String profile = "dev";
    private Integer port = 8080;
    private String dockerRepository = "your-default-repo";

    private String buildTool = "maven"; 
    private String springBootVersion = "3.2.0";
    private Set<String> dependencies = new HashSet<>();
    private Map<String, Boolean> restEndpoints;
    private Map<String, List<FieldDefinition>> entityFields;
    private boolean generateTests = true;   

    public boolean isGenerateTests() {
        return generateTests;
    }

    public void setGenerateTests(boolean generateTests) {
        this.generateTests = generateTests;
    }


    public Map<String, List<FieldDefinition>> getEntityFields() {
        return entityFields;
    }

    public void setEntityFields(Map<String, List<FieldDefinition>> entityFields) {
       this.entityFields = entityFields;
    }

    public Map<String, Boolean> getRestEndpoints() {
       return restEndpoints;
    }

    public void setRestEndpoints(Map<String, Boolean> restEndpoints) {
       this.restEndpoints = restEndpoints;
    }

    

    public CustomProjectDescription() {
        this.dependencies = new HashSet<>();
    }

    @Override
    public BuildSystem getBuildSystem() {
        return switch (buildTool) {
          case "gradle-groovy" -> new GradleBuildSystem();
          case "gradle-kotlin" -> new GradleBuildSystem(GradleBuildSystem.DIALECT_KOTLIN);
          case "maven"-> new MavenBuildSystem();
          default -> new MavenBuildSystem();
        };
    }

    public String getMavenVersion() {
       return mavenVersion;
    }

    public void setMavenVersion(String mavenVersion) {
       this.mavenVersion = mavenVersion;
    }

    @Override
    public Version getPlatformVersion() {
        return Version.parse(springBootVersion);
    }

    @Override
    public Map<String, Dependency> getRequestedDependencies() {
        Map<String, Dependency> map = new HashMap<>();

   
      Map<String, String> predefinedDeps = Map.of(
        "web", "org.springframework.boot:spring-boot-starter-web",
        "data-jpa", "org.springframework.boot:spring-boot-starter-data-jpa",
        "security", "org.springframework.boot:spring-boot-starter-security"
        
      );

      if (dependencies != null) {
        for (String id : dependencies) {
            String resolved = predefinedDeps.getOrDefault(id, id); 
            String[] parts = resolved.split(":");

            if (parts.length == 2) {
                map.put(id, Dependency.withCoordinates(parts[0], parts[1]).build());
            } else {
                throw new IllegalArgumentException("Dependency ID must be in format 'groupId:artifactId'");
            }
        }
      }

    return map;
}


    @Override
    public Packaging getPackaging() {
        return Packaging.forId("jar");
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public String getBaseDirectory() {
        return ".";
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
        updatePackageName();
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        updatePackageName();
    }

    @Override
    public String getName() {
        return name != null ? name : artifactId;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    private void updatePackageName() {
        if (groupId != null && artifactId != null) {
            this.packageName = groupId + "." + artifactId.toLowerCase();
        }
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion.split("\\.")[0]; 
    }

    @Override
    public String getApplicationName() {
        return getName();
    }

    @Override
    public String getDescription() {
        return "Custom Project Description";
    }

   

    public String getBuildTool() {
        return buildTool;
    }

    public void setBuildTool(String buildTool) {
        this.buildTool = buildTool;
    }

    public String getSpringBootVersion() {
        return springBootVersion;
    }

    public void setSpringBootVersion(String springBootVersion) {
        this.springBootVersion = springBootVersion;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<String> dependencies) {
        this.dependencies = dependencies;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getDockerRepository() {
        return dockerRepository;
    }

    public void setDockerRepository(String dockerRepository) {
        this.dockerRepository = dockerRepository;
    }

    public String getArchitectureType() {
        return architectureType;
    }

    public void setArchitectureType(String architectureType) {
        this.architectureType = architectureType;
    }

    public boolean isGenerateDocker() {
        return generateDocker;
    }

    public void setGenerateDocker(boolean generateDocker) {
        this.generateDocker = generateDocker;
    }

    public boolean isGenerateKubernetes() {
        return generateKubernetes;
    }

    public void setGenerateKubernetes(boolean generateKubernetes) {
        this.generateKubernetes = generateKubernetes;
    }

    public boolean isGenerateCLCG() {
        return generateCLCG;
    }

    public void setGenerateCLCG(boolean generateCLCG) {
        this.generateCLCG = generateCLCG;
    }

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }
}
