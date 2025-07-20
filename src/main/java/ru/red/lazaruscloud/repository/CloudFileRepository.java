package ru.red.lazaruscloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.red.lazaruscloud.model.CloudFile;

import java.util.List;
import java.util.Optional;

public interface CloudFileRepository extends JpaRepository<CloudFile, Long> {

    Optional<CloudFile> findCloudFileByServerName(String serverFileName);

    Optional<CloudFile> findCloudFileByFileOwner_IdAndServerName(Long fileOwnerId, String serverName);

    @Query("SELECT cf FROM CloudFile cf WHERE " +
            "cf.fileOwner.id = :userId AND " +
            "cf.parentId = (SELECT cf2.id FROM CloudFile cf2 WHERE " +
            "cf2.fileOwner.id = :userId AND cf2.isFolder = true AND cf2.parentId = 0) AND cf.isDeleted = false")
    List<CloudFile> findUserFilesWithRootParent(@Param("userId") Long userId);


    @Query("select cf from CloudFile cf WHERE cf.fileOwner.id = :userId AND cf.isShared = true")
    Optional<List<CloudFile>> findSharedFiles(@Param("userId") Long userId);

    @Query("select cf from CloudFile cf where cf.fileOwner.id = :ownerId AND cf.parentId = " +
            "(select cf2.id from CloudFile cf2 where cf2.serverName = :folderName) AND cf.isDeleted = false")
    Optional<List<CloudFile>> findFolderFiles(Long ownerId, String folderName);

    Optional<CloudFile> findCloudFilesByServerName(String serverName);


    Optional<List<CloudFile>> getCloudFilesByFileOwner_idAndIsDeleted(Long fileOwnerId, Boolean isDeleted);
}
