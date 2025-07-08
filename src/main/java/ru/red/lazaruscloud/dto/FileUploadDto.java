package ru.red.lazaruscloud.dto;

import org.springframework.web.multipart.MultipartFile;
import ru.red.lazaruscloud.annotation.NotEmptyFile;

public record FileUploadDto(@NotEmptyFile MultipartFile file) {}
