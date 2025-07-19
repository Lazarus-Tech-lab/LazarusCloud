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
import ru.red.lazaruscloud.dto.cloudDtos.CloudFolderDto;
import ru.red.lazaruscloud.dto.cloudDtos.FileUploadDto;
import ru.red.lazaruscloud.mapper.CloudFileMapper;
import ru.red.lazaruscloud.model.CloudFile;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.service.CloudFileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import ru.red.lazaruscloud.service.UserStorageService;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/storage")
@AllArgsConstructor
public class CloudStorageController {
    private final CloudFileService cloudFileService;
    private final UserStorageService userStorageService;

    @PostMapping(value = "/upload")
    public ResponseEntity<?> uploadFile(@AuthenticationPrincipal LazarusUserDetail userDetails, @Valid @ModelAttribute FileUploadDto fileUploadDto) {
        return new ResponseEntity<>(cloudFileService.uploadFile(userDetails, fileUploadDto.file(), fileUploadDto.parentFolderId()), HttpStatus.CREATED);
    }

    @GetMapping("/files")
    public ResponseEntity<?> getAllFiles(@AuthenticationPrincipal LazarusUserDetail userDetails) {
        return ResponseEntity.ok(cloudFileService.getAllFilesByUser(userDetails));
    }

    @PostMapping("/file/share/{fileId}")
    public ResponseEntity<?> setFileIsShared(@PathVariable long fileId, @AuthenticationPrincipal LazarusUserDetail userDetails) {
        return ResponseEntity.ok(cloudFileService.shareFile(fileId, userDetails));
    }

    @GetMapping("/download/{uuid}")
    public ResponseEntity<?> downloadPrivateFile(@AuthenticationPrincipal LazarusUserDetail userDetails, @PathVariable UUID uuid) throws MalformedURLException {
        CloudFileDto cf = cloudFileService.downloadFile(userDetails.getId(), uuid);
        if(cf != null) {
            Path fp = Paths.get(cf.path());
            Resource res = new UrlResource(fp.toUri());
            if (res.exists() || res.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cf.filename() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(res);
            } else {
                throw new RuntimeException("Файл не найден или невозможно прочитать");
            }
        }
        return ResponseEntity.notFound().build();
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


    @PostMapping(value = "/createFolder", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CloudFileDto> createFolder(@AuthenticationPrincipal LazarusUserDetail userDetail, @Valid @RequestBody CloudFolderDto cloudFolderDto) {
        CloudFile cloudFile = userStorageService.createUserFolder(userDetail, cloudFolderDto);
        System.out.println("Имя папки: " + cloudFolderDto.folderName());
        if (cloudFile != null) {
            return new ResponseEntity<>(CloudFileMapper.toDto(cloudFile), HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/getShared")
    public ResponseEntity<List<CloudFileDto>> getSharedFiles(@AuthenticationPrincipal LazarusUserDetail userDetail) {
        return new ResponseEntity<>(userStorageService.getSharedFiles(userDetail.getId()), HttpStatus.OK);
    }
}
