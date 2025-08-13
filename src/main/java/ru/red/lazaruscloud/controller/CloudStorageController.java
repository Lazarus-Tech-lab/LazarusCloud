package ru.red.lazaruscloud.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.apache.tomcat.util.file.ConfigurationSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


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

    @PostMapping("/upload/chunk")
    public ResponseEntity<?> uploadChunk(
            @AuthenticationPrincipal LazarusUserDetail user,
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam("fileName") String fileName,
            @RequestParam("fileId") String fileId,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("parentFolderId") Long parentFolderId
    ) {
        try {
            Path userDir = Paths.get("uploads", user.getRootFolder(), "chunks", fileId).normalize();
            Files.createDirectories(userDir);

            Path chunkPath = userDir.resolve(String.format("%05d.part", chunkIndex));
            if (!Files.exists(chunkPath)) {
                chunk.transferTo(chunkPath);
            }

            // Считаем сколько чанков загружено и их общий размер
            long uploadedChunks = Files.list(userDir).count();
            long uploadedSize = Files.list(userDir)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0L;
                        }
                    }).sum();
            long totalSize = totalChunks * chunk.getSize(); // приблизительно

            if (uploadedChunks == totalChunks) {
                Path finalFile = userDir.resolve("assembled.tmp");
                try (OutputStream os = Files.newOutputStream(finalFile)) {
                    for (int i = 0; i < totalChunks; i++) {
                        Path part = userDir.resolve(String.format("%05d.part", i));
                        Files.copy(part, os);
                    }
                }


                CloudFileDto saved = cloudFileService.saveChunkedFile(user, finalFile, fileName, parentFolderId);
                FileSystemUtils.deleteRecursively(userDir);

                if (saved != null) {

                    return ResponseEntity.ok(Map.of(
                            "status", "completed",
                            "file", saved
                    ));
                } else {
                    return ResponseEntity.status(400).body(Map.of(
                            "status", "error",
                            "message", "Quota exceeded or save failed"
                    ));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "status", "partial",
                    "uploadedChunks", uploadedChunks,
                    "totalChunks", totalChunks,
                    "uploadedSize", uploadedSize,
                    "fileSizeTotal", totalSize
            ));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "Chunk upload failed: " + e.getMessage()
            ));
        }
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
        if (cloudFile != null) {
            return new ResponseEntity<>(CloudFileMapper.toDto(cloudFile), HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/raw/{uuid}")
    public ResponseEntity<Resource> getRawFile(@PathVariable String uuid) throws IOException {
        CloudFile file = cloudFileService.getFileByServerName(uuid);
        Path filePath = Paths.get(file.getPath());

        MediaType type = Files.probeContentType(filePath) != null ?
                MediaType.parseMediaType(Files.probeContentType(filePath)) :
                MediaType.APPLICATION_OCTET_STREAM;

        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .contentType(type)
                .body(resource);
    }

    @GetMapping("/raw/thumb/{uuid}")
    public ResponseEntity<Resource> getThumb(@PathVariable String uuid) throws IOException {
        CloudFile file = cloudFileService.getFileByServerName(uuid);

        Path filePath = Paths.get(file.getThumbnail());

        MediaType type = Files.probeContentType(filePath) != null ?
                MediaType.parseMediaType(Files.probeContentType(filePath)) :
                MediaType.APPLICATION_OCTET_STREAM;

        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .contentType(type)
                .body(resource);
    }



    @GetMapping("/getShared")
    public ResponseEntity<List<CloudFileDto>> getSharedFiles(@AuthenticationPrincipal LazarusUserDetail userDetail) {
        return new ResponseEntity<>(userStorageService.getSharedFiles(userDetail.getId()), HttpStatus.OK);
    }

    @GetMapping("/folder/{uuid}")
    public ResponseEntity<?> getFilesFolder(@AuthenticationPrincipal LazarusUserDetail userDetail, @PathVariable UUID uuid) {
        return new ResponseEntity<>(cloudFileService.getFolderWithFiles(userDetail.getId(), uuid), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{uuid}")
    public ResponseEntity<?> softDelete(@AuthenticationPrincipal LazarusUserDetail userDetail, @PathVariable UUID uuid) {
        return new ResponseEntity<>(userStorageService.softDelete(userDetail.getId(), uuid.toString()), HttpStatus.OK);
    }

    @GetMapping("/trash")
    public ResponseEntity<?> getTrash(@AuthenticationPrincipal LazarusUserDetail userDetail) {
        return new ResponseEntity<>(userStorageService.getTrash(userDetail.getId()), HttpStatus.OK);
    }
    @GetMapping("/delete/hard/{uuid}")
    public ResponseEntity<?> hardDelete(@AuthenticationPrincipal LazarusUserDetail userDetail) {
        return null;
    }
}
