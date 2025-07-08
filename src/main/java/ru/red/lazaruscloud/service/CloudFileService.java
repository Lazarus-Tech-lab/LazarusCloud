package ru.red.lazaruscloud.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.red.lazaruscloud.dto.CloudFileDto;
import ru.red.lazaruscloud.mapper.CloudFileMapper;
import ru.red.lazaruscloud.model.CloudFile;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.repository.CloudFileRepository;

import java.util.ArrayList;
import java.util.List;

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
}
