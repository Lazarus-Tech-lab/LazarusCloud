package ru.red.lazaruscloud.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.red.lazaruscloud.dto.cloudDtos.CloudFileDto;
import ru.red.lazaruscloud.dto.cloudDtos.FileUploadDto;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.service.CloudFileService;

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

    @PostMapping("/file/share/{fileId}")
    public ResponseEntity<?> setFileIsShared(@PathVariable long fileId, @AuthenticationPrincipal LazarusUserDetail userDetails) {
        return ResponseEntity.ok(cloudFileService.shareFile(fileId, userDetails));
    }

    @GetMapping("/file/{fileName}")
    public ResponseEntity<String> getSharedFile(@PathVariable String fileName) {
        return ResponseEntity.ok(cloudFileService.getSharedFile(fileName).filename());
    }


}
