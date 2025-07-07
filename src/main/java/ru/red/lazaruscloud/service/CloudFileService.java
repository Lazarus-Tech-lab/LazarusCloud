package ru.red.lazaruscloud.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.red.lazaruscloud.dto.CloudFileDto;
import ru.red.lazaruscloud.mapper.CloudFileMapper;
import ru.red.lazaruscloud.model.CloudFile;
import ru.red.lazaruscloud.model.User;
import ru.red.lazaruscloud.repository.FileRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CloudFileService {
    private final FileRepository fileRepository;

    public List<CloudFileDto> getAllFilesByUser(Long id) {
        List<CloudFile> cloudFiles = fileRepository.findAllByFileOwnerId(id);
        List<CloudFileDto> files = new ArrayList<>();
        for (CloudFile cloudFile : cloudFiles) {
            files.add(CloudFileMapper.toDto(cloudFile));
        }
        return files;
    }
}
