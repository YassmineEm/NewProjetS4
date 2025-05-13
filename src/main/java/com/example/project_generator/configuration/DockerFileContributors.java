package com.example.project_generator.configuration;

import com.example.project_generator.model.CustomProjectDescription;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class DockerFileContributors implements ProjectContributor {

    private  CustomProjectDescription description;
    private final Configuration freemarkerConfig;

    @Autowired
    public DockerFileContributors(Configuration freemarkerConfig) {
        this.freemarkerConfig = freemarkerConfig;
    }

    @Autowired
    public void setDescription(CustomProjectDescription description) {
        this.description = description;
    }

    @Override
    public void contribute(Path projectRoot) throws IOException {
        if (description.isGenerateDocker()) {
            Files.createDirectories(projectRoot);
            Map<String, Object> model = createModel();
            Path dockerfilePath = projectRoot.resolve("Dockerfile");
            generateFromTemplate("Dockerfile.ftl", model, dockerfilePath);
        }
    }
    
    private Map<String, Object> createModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("artifactId", description.getArtifactId());
        model.put("javaVersion", description.getJavaVersion());
        return model;
    }

    private void generateFromTemplate(String templateName, 
                                    Map<String, Object> model, 
                                    Path outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(outputPath)))) {
            String content = processTemplate(templateName, model);
            writer.write(content);
        }
    }

    private String processTemplate(String templateName, 
                                 Map<String, Object> model) throws IOException {
        try (StringWriter writer = new StringWriter()) {
            Template template = freemarkerConfig.getTemplate(templateName);
            template.process(model, writer);
            return writer.toString();
        } catch (TemplateException e) {
            throw new IOException("Erreur lors du rendu du template FreeMarker : " + templateName, e);
        }
    }
    public String generateContent() throws IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("artifactId", description.getArtifactId());
        model.put("javaVersion", description.getJavaVersion());
        
        try (StringWriter writer = new StringWriter()) {
            Template template = freemarkerConfig.getTemplate("Dockerfile.ftl");
            template.process(model, writer);
            return writer.toString();
        } catch (TemplateException e) {
            throw new IOException("Erreur lors de la génération du Dockerfile", e);
        }
    }
}