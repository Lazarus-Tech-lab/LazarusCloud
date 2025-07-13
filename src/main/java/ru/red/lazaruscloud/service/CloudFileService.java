package ru.red.lazaruscloud.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.red.lazaruscloud.dto.cloudDtos.CloudFileDto;
import ru.red.lazaruscloud.mapper.CloudFileMapper;
import ru.red.lazaruscloud.model.CloudFile;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.model.User;
import ru.red.lazaruscloud.repository.CloudFileRepository;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@AllArgsConstructor
public class CloudFileService {
    private final CloudFileRepository cloudFileRepository;

    public List<CloudFileDto> getAllFilesByUser(LazarusUserDetail userDetails) {
        List<CloudFile> cloudFiles = cloudFileRepository.findAllByFileOwner_Id(userDetails.getId());
        List<CloudFileDto> files = new ArrayList<>();
        for (CloudFile cloudFile : cloudFiles) {
            files.add(CloudFileMapper.toDto(cloudFile));
        }
        return files;
    }

    public CloudFileDto uploadFile(LazarusUserDetail userDetails, MultipartFile file) {

        try {
            Path uploadDir = Paths.get("uploads");
            Files.createDirectories(uploadDir);
            String serverFileName = UUID.randomUUID().toString();
            String serverFileNameExt = UUID.randomUUID() + Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
            Path filePath = uploadDir.resolve(Objects.requireNonNull(serverFileNameExt));
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            CloudFile cloudFile = new CloudFile();
            cloudFile.setFileName(file.getOriginalFilename());
            cloudFile.setPath(filePath.toString());
            cloudFile.setFileSize(file.getSize());
            cloudFile.setServerFileName(serverFileName);
            User u = new User();
            u.setId(userDetails.getId());
            cloudFile.setFileOwner(u);

            CloudFile uploadedFile = cloudFileRepository.save(cloudFile);

            return new CloudFileDto(uploadedFile.getId(), uploadedFile.getFileName(), uploadedFile.getServerFileName(),
                    uploadedFile.getFileOwner().getId(), uploadedFile.getFileSize(),
                    uploadedFile.isShared(), uploadedFile.getPath());
        } catch (IOException e) {
            return null;
        }


    }

    public CloudFile shareFile(long fileId, LazarusUserDetail userDetail) {
        Optional<CloudFile> file = cloudFileRepository.findById(fileId);

        if(file.isPresent()) {
            CloudFile uploadedFile = file.get();
            if (uploadedFile.getFileOwner().getId() == userDetail.getId()) {
                uploadedFile.setShared(true);
                return cloudFileRepository.save(uploadedFile);
            }

        }
        return null;
    }

    public Optional<CloudFileDto> getSharedFile(String fileName) {
        Optional<CloudFile> file = cloudFileRepository.findCloudFileByServerFileName(fileName);
        if (file.isPresent() && file.get().isShared()) {
            return Optional.of(CloudFileMapper.toDto(file.get()));
        }
        return Optional.empty();
    }
}
