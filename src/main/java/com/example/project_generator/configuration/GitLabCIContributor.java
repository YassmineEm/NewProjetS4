package com.example.project_generator.configuration;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.project_generator.model.CustomProjectDescription;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class GitLabCIContributor implements ProjectContributor {

    @Autowired
    private Configuration freemarkerConfig;

    @Autowired
    private CustomProjectDescription description;

    private String dockerRepository;

    @Override
    public void contribute(Path projectRoot) throws IOException {
        if (description.getArtifactId() == null || description.getArtifactId().isEmpty()) {
            throw new IllegalArgumentException("Artifact ID cannot be null or empty");
        }
        Files.createDirectories(projectRoot);
        Map<String, Object> model = new HashMap<>();
        model.put("artifactId", description.getArtifactId());
        model.put("javaVersion", description.getJavaVersion() != null ? description.getJavaVersion() : "17");
        model.put("dockerRepository", 
            description.getDockerRepository() != null ? 
            description.getDockerRepository() : 
            "your-default-repo");

        Path gitlabCiPath = projectRoot.resolve(".gitlab-ci.yml");
        generateFromTemplate(".gitlab-ci.yml.ftl", model, gitlabCiPath);
    }

    private void generateFromTemplate(String templateName, Map<String, Object> model, Path outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outputPath)))) {
            Template template = freemarkerConfig.getTemplate(templateName);
            template.process(model, writer);
        } catch (TemplateException e) {
            throw new IOException("Erreur lors de la génération du template GitLab CI", e);
        }
    }
    public String generateContent() throws IOException {
        if (description.getArtifactId() == null || description.getArtifactId().isEmpty()) {
           throw new IllegalArgumentException("Artifact ID cannot be null or empty");
        }

        Map<String, Object> model = new HashMap<>();
        model.put("artifactId", description.getArtifactId());
        model.put("javaVersion", description.getJavaVersion() != null ? description.getJavaVersion() : "17");
        model.put("dockerRepository", 
           description.getDockerRepository() != null ? 
           description.getDockerRepository() : 
           "your-default-repo");

        try (StringWriter writer = new StringWriter()) {
          Template template = freemarkerConfig.getTemplate(".gitlab-ci.yml.ftl");
          template.process(model, writer);
          return writer.toString();
        } catch (TemplateException e) {
           throw new IOException("Erreur lors de la génération du .gitlab-ci.yml", e);
        }
    }
    public void setDescription(CustomProjectDescription description) {
        this.description = description;
    }
    
}