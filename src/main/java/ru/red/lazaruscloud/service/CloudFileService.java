package ru.red.lazaruscloud.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.red.lazaruscloud.dto.cloudDtos.CloudFileDto;
import ru.red.lazaruscloud.dto.cloudDtos.CloudFolderDto;
import ru.red.lazaruscloud.dto.cloudDtos.FolderDto;
import ru.red.lazaruscloud.mapper.CloudFileMapper;
import ru.red.lazaruscloud.model.CloudFile;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.model.StorageLimit;
import ru.red.lazaruscloud.model.User;
import ru.red.lazaruscloud.repository.CloudFileRepository;
import ru.red.lazaruscloud.util.thumbnails.ThumbnailsHelper;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CloudFileService {

    @Value("${application.upload.path}")
    private String UPLOAD_PATH;

    private final CloudFileRepository cloudFileRepository;
    private final CloudQuotaService cloudQuotaService;

    public CloudFileService(CloudFileRepository cloudFileRepository, CloudQuotaService cloudQuotaService) {
        this.cloudFileRepository = cloudFileRepository;
        this.cloudQuotaService = cloudQuotaService;
    }

    public List<CloudFileDto> getAllFilesByUser(LazarusUserDetail userDetails) {

        List<CloudFileDto> resultFiles = new ArrayList<>();
        List<CloudFile> files = cloudFileRepository.findUserFilesWithRootParent(userDetails.getId());

        files.forEach(file -> {
            resultFiles.add(CloudFileMapper.toDto(file));
        });

        return resultFiles;
    }

    public Long getRootFolderId(Long userId) {
        CloudFile cf = cloudFileRepository.findCloudFilesByFileOwner_IdAndParentId(userId, 0L);
        return cf.getId();
    }

    public CloudFileDto uploadFile(LazarusUserDetail userDetails, MultipartFile file, long parentId) {
        //TODO: bug fix parentId
        try {
            Path uploadDir = Paths.get(UPLOAD_PATH, userDetails.getRootFolder());
            String serverFileName = UUID.randomUUID().toString();
            String serverFileNameExt = UUID.randomUUID() + Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
            Path filePath = uploadDir.resolve(Objects.requireNonNull(serverFileNameExt));
            CloudFile cloudFile = new CloudFile();
            cloudFile.setName(file.getOriginalFilename());
            cloudFile.setPath(filePath.toString());
            cloudFile.setFileSize(file.getSize());
            cloudFile.setServerName(serverFileName);
            cloudFile.setParentId(parentId);
            cloudFile.setIsFolder(false);
            User u = new User();
            u.setId(userDetails.getId());
            StorageLimit quota = cloudQuotaService.getQuota(u);
            cloudFile.setFileOwner(u);
            long newUserQuota = quota.getQuotaUsedLimit() + file.getSize();
            quota.setQuotaUsedLimit(newUserQuota);
            if (quota.getQuotaLimit() >= newUserQuota) {
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                CloudFile uploadedFile = cloudFileRepository.save(cloudFile);
                cloudQuotaService.setUsedQuota(quota);

                BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
                System.out.println(attr);
                return CloudFileMapper.toDto(uploadedFile);
            } else {
                return null;
            }

        } catch (IOException e) {
            return null;
        }
    }

    public CloudFile shareFile(long fileId, LazarusUserDetail userDetail) {
        Optional<CloudFile> file = cloudFileRepository.findById(fileId);

        if (file.isPresent()) {
            CloudFile uploadedFile = file.get();
            if (uploadedFile.getFileOwner().getId() == userDetail.getId()) {
                uploadedFile.setShared(true);
                return cloudFileRepository.save(uploadedFile);
            }
        }
        return null;
    }

    public Optional<CloudFileDto> getSharedFile(String fileName) {
        Optional<CloudFile> file = cloudFileRepository.findCloudFileByServerName(fileName);
        if (file.isPresent() && file.get().isShared()) {
            return Optional.of(CloudFileMapper.toDto(file.get()));
        }
        return Optional.empty();
    }

    public boolean createPhysicalFolder(User user, CloudFolderDto cloudFolderDto) {
        Path folderPath = Paths.get(UPLOAD_PATH, cloudFolderDto.folderName());
        try {
            Files.createDirectories(folderPath);
            CloudFile cloudFile = new CloudFile();
            cloudFile.setFileOwner(user);
            cloudFile.setPath(folderPath.toString());
            cloudFile.setName(cloudFolderDto.folderName());
            cloudFile.setServerName(UUID.randomUUID().toString());
            cloudFile.setIsFolder(true);
            cloudFile.setShared(false);
            cloudFile.setIsDeleted(false);
            cloudFile.setParentId(0L);
            cloudFile.setThumbnail("");
            cloudFileRepository.save(cloudFile);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CloudFile createVirtualFolder(LazarusUserDetail user, CloudFolderDto cloudFolderDto) {
        String sanitizedFolderName = sanitizeFolderName(cloudFolderDto.folderName());

        Path userBasePath = Paths.get(UPLOAD_PATH, String.valueOf(user.getId())).normalize();
        Path folderPath = userBasePath.resolve(sanitizedFolderName).normalize();

        if (!folderPath.startsWith(userBasePath)) {
            throw new SecurityException("User: " + user.getUsername() + " пробовал выйти из песочницы");
        }

        CloudFile cloudFile = new CloudFile();
        User u = new User();
        u.setId(user.getId());

        cloudFile.setFileOwner(u);
        cloudFile.setPath(folderPath.toString());
        cloudFile.setName(sanitizedFolderName);
        cloudFile.setServerName(UUID.randomUUID().toString());
        cloudFile.setIsFolder(true);
        cloudFile.setShared(false);
        cloudFile.setIsDeleted(false);
        cloudFile.setParentId(cloudFolderDto.parentId());
        cloudFile.setThumbnail("uploads/pictures/folder.png");

        return cloudFileRepository.save(cloudFile);
    }

    public List<CloudFileDto> getSharedFilesByOwnerId(Long ownerId) {
        Optional<List<CloudFile>> files = cloudFileRepository.findSharedFiles(ownerId);
        return files.map(cloudFiles -> cloudFiles.stream().map(CloudFileMapper::toDto).collect(Collectors.toList())).orElseGet(ArrayList::new);
    }


    private String sanitizeFolderName(String name) {
        return name.replaceAll("[^\\p{L}\\p{N}_\\- ]", "");
    }

    public CloudFileDto downloadFile(Long ownerId, UUID uuid) {
        Optional<CloudFile> file = cloudFileRepository.findCloudFileByFileOwner_IdAndServerName(ownerId, uuid.toString());
        if (file.isPresent()) {
            CloudFile uploadedFile = file.get();
            return CloudFileMapper.toDto(uploadedFile);
        }
        return null;
    }

    public List<CloudFileDto> getFolderFiles(Long ownerId, UUID folderId) {
        Optional<List<CloudFile>> files = cloudFileRepository.findFolderFiles(ownerId, folderId.toString());
        return files.map(cloudFiles -> cloudFiles.stream().map(CloudFileMapper::toDto).collect(Collectors.toList())).orElseGet(ArrayList::new);
    }

    public FolderDto getFolderWithFiles(Long ownerId, UUID folderUuid) {
        Optional<CloudFile> folder = cloudFileRepository.findCloudFilesByServerName(folderUuid.toString());
        if (folder.isEmpty()) {
            throw new RuntimeException("Folder not found");
        }

        Long parentId = folder.get().getId();

        List<CloudFileDto> fileDtos = cloudFileRepository.findFolderFiles(ownerId, folderUuid.toString())
                .orElseGet(ArrayList::new)
                .stream()
                .map(CloudFileMapper::toDto)
                .collect(Collectors.toList());

        return new FolderDto(parentId, folderUuid, fileDtos);
    }

    public CloudFileDto saveChunkedFile(LazarusUserDetail userDetails, Path assembledFile, String originalName, Long parentId) {

        try {
            Path uploadDir = Paths.get(UPLOAD_PATH, userDetails.getRootFolder());
            Files.createDirectories(uploadDir);

            String serverFileName = UUID.randomUUID().toString();
            String ext = "";

            if (originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }

            String serverFileNameExt = UUID.randomUUID() + ext;
            Path filePath = uploadDir.resolve(serverFileNameExt);
            Files.move(assembledFile, filePath, StandardCopyOption.REPLACE_EXISTING);
            String pathToThumbnail = ThumbnailsHelper.getThumbnailFromFile(filePath, userDetails.getRootFolder(), serverFileName);

            CloudFile cloudFile = new CloudFile();
            cloudFile.setName(originalName);
            cloudFile.setPath(filePath.toString());
            cloudFile.setFileSize(Files.size(filePath));
            cloudFile.setServerName(serverFileName);
            cloudFile.setParentId(parentId);
            cloudFile.setIsFolder(false);
            cloudFile.setIsDeleted(false);
            cloudFile.setThumbnail(pathToThumbnail);

            User u = new User();
            u.setId(userDetails.getId());

            StorageLimit quota = cloudQuotaService.getQuota(u);
            long newUserQuota = quota.getQuotaUsedLimit() + Files.size(filePath);
            if (quota.getQuotaLimit() < newUserQuota) {
                Files.deleteIfExists(filePath);
                return null;
            }

            cloudFile.setFileOwner(u);
            CloudFile uploadedFile = cloudFileRepository.save(cloudFile);
            quota.setQuotaUsedLimit(newUserQuota);
            cloudQuotaService.setUsedQuota(quota);

            return CloudFileMapper.toDto(uploadedFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public CloudFileDto softDelete(long ownerId, String uuid) {
        Optional<CloudFile> cfgto = cloudFileRepository.findCloudFileByFileOwner_IdAndServerName(ownerId, uuid);
        if (cfgto.isPresent()) {
            CloudFile uploadedFile = cfgto.get();
            uploadedFile.setIsDeleted(true);
            cloudFileRepository.save(uploadedFile);

            return CloudFileMapper.toDto(uploadedFile);
        }

        return null;
    }

    public List<CloudFileDto> getTrash(long ownerId) {
        Optional<List<CloudFile>> files = cloudFileRepository.getCloudFilesByFileOwner_idAndIsDeleted(ownerId, true);
        if (files.isPresent()) {
            List<CloudFile> fileList = files.get();
            return fileList.stream().map(CloudFileMapper::toDto).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public CloudFile getFileByServerName(String uuid) {
        Optional<CloudFile> cf = cloudFileRepository.findCloudFilesByServerName(uuid);
        return cf.orElse(null);
    }
}
