package ru.red.lazaruscloud.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.red.lazaruscloud.dto.CloudFileDto;
import ru.red.lazaruscloud.dto.FileUploadDto;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.repository.CloudFileRepository;
import ru.red.lazaruscloud.repository.UserRepository;
import ru.red.lazaruscloud.service.CloudFileService;
import ru.red.lazaruscloud.service.UserService;

@RestController
@RequestMapping("/api/storage")
@AllArgsConstructor
public class CloudFileController {
    private final CloudFileService cloudFileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@AuthenticationPrincipal LazarusUserDetail userDetails, @Valid @ModelAttribute FileUploadDto fileUploadDto) {
        return new ResponseEntity<>(cloudFileService.uploadFile(userDetails, fileUploadDto.file()), HttpStatus.CREATED);
    }

    @GetMapping("/files")
    public ResponseEntity<?> getAllFiles(@AuthenticationPrincipal LazarusUserDetail userDetails) {
        return ResponseEntity.ok(cloudFileService.getAllFilesByUser(userDetails));
    }
}
