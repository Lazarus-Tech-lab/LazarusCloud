package ru.red.lazaruscloud.dto;

public record UserDataDto(Long id, String username, long quota, long usedQuota) {
}
