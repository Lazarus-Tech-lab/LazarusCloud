package ru.red.lazaruscloud.dto.cloudDtos;

public record CloudFileDto (long Id, String filename,
                            long ownerId, double fileSize,
                            boolean isShared, String path){}
