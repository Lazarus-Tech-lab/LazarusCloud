package ru.red.lazaruscloud.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
    public long uploadFile(@AuthenticationPrincipal LazarusUserDetail userDetails, @RequestParam("file")  MultipartFile file) {
        return userDetails.getId();
    }

    @GetMapping("/files")
    public ResponseEntity<?> getMyFiles(@AuthenticationPrincipal LazarusUserDetail userDetails) {
        return ResponseEntity.ok(cloudFileService.getAllFilesByUser(userDetails));
    }
}
