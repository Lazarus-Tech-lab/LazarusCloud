package ru.red.lazaruscloud.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.red.lazaruscloud.dto.cloudDtos.CloudFileDto;
import ru.red.lazaruscloud.dto.cloudDtos.CloudFolderDto;
import ru.red.lazaruscloud.mapper.CloudFileMapper;
import ru.red.lazaruscloud.model.CloudFile;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.model.User;
import ru.red.lazaruscloud.repository.CloudFileRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class CloudFileService {

    @Value("${application.upload.path}")
    private String UPLOAD_PATH;

    private final CloudFileRepository cloudFileRepository;

    public CloudFileService(CloudFileRepository cloudFileRepository) {
        this.cloudFileRepository = cloudFileRepository;
    }

    public List<CloudFileDto> getAllFilesByUser(LazarusUserDetail userDetails) {
        List<CloudFile> cloudFiles = cloudFileRepository.findAllByFileOwner_Id(userDetails.getId());
        List<CloudFileDto> files = new ArrayList<>();
        for (CloudFile cloudFile : cloudFiles) {
            files.add(CloudFileMapper.toDto(cloudFile));
        }
        return files;
    }

    public CloudFileDto uploadFile(LazarusUserDetail userDetails, MultipartFile file, long parentId) {
        //TODO: bug fix parentId
        try {
            Path uploadDir = Paths.get(UPLOAD_PATH, userDetails.getRootFolder());
            String serverFileName = UUID.randomUUID().toString();
            String serverFileNameExt = UUID.randomUUID() + Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
            Path filePath = uploadDir.resolve(Objects.requireNonNull(serverFileNameExt));
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            CloudFile cloudFile = new CloudFile();
            cloudFile.setName(file.getOriginalFilename());
            cloudFile.setPath(filePath.toString());
            cloudFile.setFileSize(file.getSize());
            cloudFile.setServerName(serverFileName);
            cloudFile.setParentId(parentId);
            cloudFile.setIsFolder(false);
            User u = new User();
            u.setId(userDetails.getId());
            cloudFile.setFileOwner(u);

            CloudFile uploadedFile = cloudFileRepository.save(cloudFile);

            return CloudFileMapper.toDto(uploadedFile);
        } catch (IOException e) {
            return null;
        }
    }

    public CloudFile shareFile(long fileId, LazarusUserDetail userDetail) {
        Optional<CloudFile> file = cloudFileRepository.findById(fileId);

        if (file.isPresent()) {
            CloudFile uploadedFile = file.get();
            if (uploadedFile.getFileOwner().getId() == userDetail.getId()) {
                uploadedFile.setShared(true);
                return cloudFileRepository.save(uploadedFile);
            }
        }
        return null;
    }

    public Optional<CloudFileDto> getSharedFile(String fileName) {
        Optional<CloudFile> file = cloudFileRepository.findCloudFileByServerName(fileName);
        if (file.isPresent() && file.get().isShared()) {
            return Optional.of(CloudFileMapper.toDto(file.get()));
        }
        return Optional.empty();
    }

    public boolean createPhysicalFolder(User user, CloudFolderDto cloudFolderDto) {
        Path folderPath = Paths.get(UPLOAD_PATH, cloudFolderDto.folderName());
        try {
            Files.createDirectories(folderPath);
            CloudFile cloudFile = new CloudFile();
            cloudFile.setFileOwner(user);
            cloudFile.setPath(folderPath.toString());
            cloudFile.setName(cloudFolderDto.folderName());
            cloudFile.setServerName(UUID.randomUUID().toString());
            cloudFile.setIsFolder(true);
            cloudFile.setShared(false);
            cloudFile.setIsDeleted(false);
            cloudFile.setParentId(cloudFolderDto.parentId());
            cloudFileRepository.save(cloudFile);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CloudFile createVirtualFolder(LazarusUserDetail user, CloudFolderDto cloudFolderDto) {
        String sanitizedFolderName = sanitizeFolderName(cloudFolderDto.folderName());

        Path userBasePath = Paths.get(UPLOAD_PATH, String.valueOf(user.getId())).normalize();
        Path folderPath = userBasePath.resolve(sanitizedFolderName).normalize();

        if (!folderPath.startsWith(userBasePath)) {
            throw new SecurityException("User: " + user.getUsername() + " пробовал выйти из песочницы");
        }

        CloudFile cloudFile = new CloudFile();
        User u = new User();
        u.setId(user.getId());

        cloudFile.setFileOwner(u);
        cloudFile.setPath(folderPath.toString());
        cloudFile.setName(sanitizedFolderName);
        cloudFile.setServerName(UUID.randomUUID().toString());
        cloudFile.setIsFolder(true);
        cloudFile.setShared(false);
        cloudFile.setIsDeleted(false);
        cloudFile.setParentId(cloudFolderDto.parentId());

        return cloudFileRepository.save(cloudFile);
    }

    private String sanitizeFolderName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "");
    }
}
