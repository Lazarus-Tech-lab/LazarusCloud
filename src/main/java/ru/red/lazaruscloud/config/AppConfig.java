package ru.red.lazaruscloud.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AppConfig {

    @Value("${application.upload.path}")
    private String uploadFolderPath;

    @PostConstruct
    public void createUploadFolder() {
        Path uploadPath = Paths.get(uploadFolderPath);
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            System.out.println("Uplaod folder already exists: " + uploadFolderPath);
        }

    }
}
