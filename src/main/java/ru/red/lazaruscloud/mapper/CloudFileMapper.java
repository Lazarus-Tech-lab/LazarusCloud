package ru.red.lazaruscloud.mapper;

import ru.red.lazaruscloud.dto.cloudDtos.CloudFileDto;
import ru.red.lazaruscloud.model.CloudFile;

public class CloudFileMapper {
    public static CloudFileDto toDto(CloudFile cloudFile) {
        return new CloudFileDto(cloudFile.getId(), cloudFile.getFileName(), cloudFile.getServerFileName(),
                cloudFile.getFileOwner().getId(), cloudFile.getFileSize(),
                cloudFile.isShared(), cloudFile.getPath());
    }
}
