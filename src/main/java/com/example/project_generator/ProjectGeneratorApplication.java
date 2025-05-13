package com.example.project_generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ProjectGeneratorApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE); 
        app.run(args);
	}

}
