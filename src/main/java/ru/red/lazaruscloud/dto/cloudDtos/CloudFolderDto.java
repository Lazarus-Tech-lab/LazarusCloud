package ru.red.lazaruscloud.dto.cloudDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CloudFolderDto(
        @NotNull
        Long parentId,
        @NotBlank
        @Size(min = 5, max = 127)
        String folderName) {
}
