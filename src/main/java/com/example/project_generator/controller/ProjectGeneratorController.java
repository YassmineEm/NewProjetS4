package com.example.project_generator.controller;

import com.example.project_generator.model.CustomProjectRequest;
import com.example.project_generator.model.CustomProjectDescription;
import com.example.project_generator.converter.CustomProjectRequestToDescriptionConverter;
import com.example.project_generator.service.ProjectGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/generate")
public class ProjectGeneratorController {

    @Autowired
    private CustomProjectRequestToDescriptionConverter converter;

    @Autowired
    private ProjectGenerationService projectGenerationService;

    @PostMapping
    public ResponseEntity<byte[]> generateProject(@RequestBody CustomProjectRequest request) throws IOException {
        CustomProjectDescription description = converter.convert(request);
    
      
        String projectPath = projectGenerationService.generateProject(description);
    
      
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
           Path projectDir = Paths.get(projectPath); // Supprimer les :
           addDirectoryToZip(zipOut, projectDir, projectDir);
        } catch (Exception e) {
           throw new IOException("Failed to create ZIP file: " + e.getMessage());
        }
    
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=" + description.getArtifactId() + ".zip")
            .body(baos.toByteArray());
    }

    private void addDirectoryToZip(ZipOutputStream zipOut, Path rootPath, Path currentPath) throws IOException {
        for (Path path : Files.newDirectoryStream(currentPath)) {
            if (Files.isDirectory(path)) {
                addDirectoryToZip(zipOut, rootPath, path);
            } else {
                String zipEntryName = rootPath.relativize(path).toString().replace("\\", "/");
                ZipEntry zipEntry = new ZipEntry(zipEntryName);
                zipOut.putNextEntry(zipEntry);
                zipOut.write(Files.readAllBytes(path));
                zipOut.closeEntry();
            }
        }
    }
}