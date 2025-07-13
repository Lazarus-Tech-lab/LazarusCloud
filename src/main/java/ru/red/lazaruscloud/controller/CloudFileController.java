package ru.red.lazaruscloud.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.file.ConfigurationSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.red.lazaruscloud.dto.cloudDtos.CloudFileDto;
import ru.red.lazaruscloud.dto.cloudDtos.FileUploadDto;
import ru.red.lazaruscloud.model.CloudFile;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.service.CloudFileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.ClassPathResource;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


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
    public ResponseEntity<Resource> getSharedFile(@PathVariable String fileName) throws MalformedURLException {
        Optional<CloudFileDto> cf = cloudFileService.getSharedFile(fileName);
        if(cf.isPresent()) {
            Path fp = Paths.get(cf.get().path());
            Resource res = new UrlResource(fp.toUri());
            if (res.exists() || res.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cf.get().filename() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(res);
            } else {
                throw new RuntimeException("Файл не найден или невозможно прочитать");
            }
        }

        return ResponseEntity.notFound().build();

    }


}
