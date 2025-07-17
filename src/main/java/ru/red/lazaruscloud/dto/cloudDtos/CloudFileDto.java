package ru.red.lazaruscloud.dto.cloudDtos;

public record CloudFileDto (long Id, String filename, String serverName,
                            long ownerId, double size, Boolean isFolder,
                            boolean isShared, String path){}
