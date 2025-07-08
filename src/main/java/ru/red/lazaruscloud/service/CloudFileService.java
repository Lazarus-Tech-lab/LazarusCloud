package ru.red.lazaruscloud.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.red.lazaruscloud.dto.CloudFileDto;
import ru.red.lazaruscloud.mapper.CloudFileMapper;
import ru.red.lazaruscloud.model.CloudFile;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.model.User;
import ru.red.lazaruscloud.repository.CloudFileRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class CloudFileService {
    private final CloudFileRepository cloudFileRepository;

    public List<CloudFileDto> getAllFilesByUser(LazarusUserDetail userDetails) {
        List<CloudFile> cloudFiles = cloudFileRepository.findAllByFileOwner(userDetails);
        List<CloudFileDto> files = new ArrayList<>();
        for (CloudFile cloudFile : cloudFiles) {
            files.add(CloudFileMapper.toDto(cloudFile));
        }
        return files;
    }

    public CloudFileDto uploadFile (LazarusUserDetail userDetails, MultipartFile file)  {

        try {
            Path uploadDir = Paths.get("uploads");
            Files.createDirectories(uploadDir);

            Path filePath = uploadDir.resolve(Objects.requireNonNull(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            CloudFile cloudFile = new CloudFile();
            cloudFile.setFileName(file.getOriginalFilename());
            cloudFile.setPath(filePath.toString());
            cloudFile.setFileSize(file.getSize());
            User u = new User();
            u.setId(userDetails.getId());
            cloudFile.setFileOwner(u);

            CloudFile uploadedFile = cloudFileRepository.save(cloudFile);

            return new CloudFileDto(uploadedFile.getId(), uploadedFile.getFileName(),
                    uploadedFile.getFileOwner().getId(), uploadedFile.getFileSize(),
                    uploadedFile.isShared(), uploadedFile.getPath() );
        } catch (IOException e) {
            return null;
        }
    }
}
