package ru.red.lazaruscloud.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.red.lazaruscloud.dto.CloudFileDto;
import ru.red.lazaruscloud.model.CloudFile;
import ru.red.lazaruscloud.model.User;
import ru.red.lazaruscloud.repository.FileRepository;
import ru.red.lazaruscloud.repository.UserRepository;
import ru.red.lazaruscloud.service.CloudFileService;
import ru.red.lazaruscloud.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/storage")
@AllArgsConstructor
public class FileController {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final UserService userService;
    private final CloudFileService cloudFileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("file")  MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Файл пустой"));
        }

        try {
            // Пример: сохраняем файл во временную директорию
            Path uploadDir = Paths.get("uploads");
            Files.createDirectories(uploadDir); // если нет директории — создать

            Path filePath = uploadDir.resolve(Objects.requireNonNull(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Optional<User> u = userRepository.findUserByUsername(userDetails.getUsername());

            if(u.isPresent()) {
                CloudFile cloudFile = new CloudFile();
                cloudFile.setFileOwner(u.get());
                cloudFile.setFileName(filePath.getFileName().toString());
                cloudFile.setFileSize(file.getSize());
                cloudFile.setPath(filePath.toString());

                fileRepository.save(cloudFile);
                return ResponseEntity.ok(Map.of(
                        "fileName", file.getOriginalFilename(),
                        "size", file.getSize(),
                        "path", filePath.toString()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("null","null"));
            }
            
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при сохранении файла"));
        }
    }

    @GetMapping("/files")
    public ResponseEntity<?> getMyFiles(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> user = userService.getUserByUsername(userDetails.getUsername());
        if(user.isPresent()) {
            List<CloudFileDto> files = cloudFileService.getAllFilesByUser(user.get().getId());
            return ResponseEntity.ok(files);
        }
        return ResponseEntity.badRequest().body(Map.of("null","null"));

    }
}
