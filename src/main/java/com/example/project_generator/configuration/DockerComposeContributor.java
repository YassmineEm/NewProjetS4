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
public class DockerComposeContributor implements ProjectContributor {

    @Autowired
    private Configuration freemarkerConfig;

    @Autowired
    private CustomProjectDescription description;

    @Override
    public void contribute(Path projectRoot) throws IOException {
        Files.createDirectories(projectRoot);
        Map<String, Object> model = new HashMap<>();
        model.put("serviceName", "app");
        model.put("port", String.valueOf(description.getPort() != null ? description.getPort() : 8080));
        model.put("artifactId", description.getArtifactId());
        model.put("profile", description.getProfile() != null ? description.getProfile() : "dev");

        Path composePath = projectRoot.resolve("docker-compose.yml");
        generateFromTemplate("docker-compose.ftl", model, composePath);
    }

    private void generateFromTemplate(String templateName, Map<String, Object> model, Path outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outputPath)))) {
            Template template = freemarkerConfig.getTemplate(templateName);
            template.process(model, writer);
        } catch (TemplateException e) {
            throw new IOException("Erreur lors de la génération du template docker-compose", e);
        }
    }
    public String generateContent() throws IOException {
       Map<String, Object> model = new HashMap<>();
       model.put("serviceName", "app");
       model.put("port", description.getPort() != null ? String.valueOf(description.getPort()) : 8080);
       model.put("artifactId", description.getArtifactId());
       model.put("profile", description.getProfile() != null ? description.getProfile() : "dev");

        try (StringWriter writer = new StringWriter()) {
          Template template = freemarkerConfig.getTemplate("docker-compose.ftl");
          template.process(model, writer);
          return writer.toString();
        } catch (TemplateException e) {
          throw new IOException("Erreur lors de la génération du docker-compose.yml", e);
        }
   }
   public void setDescription(CustomProjectDescription description) {
    this.description = description;
}

}
