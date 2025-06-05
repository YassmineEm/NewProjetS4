package com.example.project_generator.converter;

import com.example.project_generator.model.CustomProjectRequest;
import com.example.project_generator.model.CustomProjectDescription;
import org.springframework.stereotype.Component;

@Component
public class CustomProjectRequestToDescriptionConverter {
    public CustomProjectDescription convert(CustomProjectRequest request) {
        CustomProjectDescription description = new CustomProjectDescription();
        description.setArchitectureType(request.getArchitectureType());
        description.setGenerateDocker(request.isGenerateDocker());
        description.setGenerateKubernetes(request.isGenerateKubernetes());
        description.setGenerateCLCG(request.isGenerateCLCG());
        description.setEntities(request.getEntities());
        description.setDockerRepository(request.getDockerRepository());
        description.setArtifactId(request.getArtifactId());
        description.setName(request.getName());
        description.setJavaVersion(request.getJavaVersion());
        description.setPort(request.getPort());
        description.setProfile(request.getProfile());
        description.setBuildTool(request.getBuildTool());
        description.setSpringBootVersion(request.getSpringBootVersion());
        description.setDependencies(request.getDependencies());
        description.setGroupId(request.getGroupId() != null ? request.getGroupId() : "com.example");
        description.setMavenVersion(request.getMavenVersion());
        description.setEntityFields(request.getEntityFields());
        description.setRestEndpoints(request.getRestEndpoints());
        description.setGenerateTests(request.isGenerateTests());
        return description;
    }
}