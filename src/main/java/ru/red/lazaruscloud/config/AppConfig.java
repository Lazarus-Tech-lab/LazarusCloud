package ru.red.lazaruscloud.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Configuration
public class AppConfig {

    @Value("${application.upload.path}")
    private String uploadFolderPath;

    private final String resourceFolder = uploadFolderPath + "/pictures";

    @PostConstruct
    public void createUploadFolder() {
        Path uploadPath = Paths.get(uploadFolderPath);
        try {
            Files.createDirectories(uploadPath);

            copyFolderFromResources("static/pictures", String.valueOf(uploadPath+"/pictures"));
        } catch (IOException e) {
            System.out.println("Uplaod folder already exists: " + uploadFolderPath);
        }

    }


    public void copyFolderFromResources(String sourceFolder, String targetFolder) throws IOException {
        // Проверяем, что исходная папка существует
        ClassPathResource sourceResource = new ClassPathResource(sourceFolder);
        if (!sourceResource.exists()) {
            throw new IOException("Папка не найдена в ресурсах: " + sourceFolder);
        }

        // Получаем путь к папке внутри JAR или файловой системы
        Path sourcePath = Paths.get(sourceResource.getURI());
        Path targetPath = Paths.get(targetFolder);

        // Если это директория, копируем рекурсивно
        if (Files.isDirectory(sourcePath)) {
            try (Stream<Path> paths = Files.walk(sourcePath)) {
                paths.forEach(source -> {
                    try {
                        Path destination = targetPath.resolve(sourcePath.relativize(source));
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(destination); // Создаём подпапки
                        } else {
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Ошибка копирования: " + source, e);
                    }
                });
            }
        } else {
            throw new IOException("Указанный ресурс не является папкой: " + sourceFolder);
        }
    }
}
