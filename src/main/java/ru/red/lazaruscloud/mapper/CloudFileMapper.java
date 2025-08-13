package ru.red.lazaruscloud.mapper;

import ru.red.lazaruscloud.dto.cloudDtos.CloudFileDto;
import ru.red.lazaruscloud.model.CloudFile;

public class CloudFileMapper {
    public static CloudFileDto toDto(CloudFile cloudFile) {
        return new CloudFileDto(cloudFile.getId(), cloudFile.getName(), cloudFile.getServerName(),
                cloudFile.getFileOwner().getId(), cloudFile.getFileSize(), cloudFile.getIsFolder(),
                cloudFile.isShared(), cloudFile.getPath(), cloudFile.getThumbnail());
    }
}
