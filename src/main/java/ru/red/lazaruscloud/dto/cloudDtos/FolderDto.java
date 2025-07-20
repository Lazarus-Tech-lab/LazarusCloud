package ru.red.lazaruscloud.dto.cloudDtos;

import java.util.List;
import java.util.UUID;

public record FolderDto(Long id, UUID serverName, List<CloudFileDto> fileDtos) {
}
