package ru.red.lazaruscloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.red.lazaruscloud.model.CloudFile;
import ru.red.lazaruscloud.model.User;

import java.util.List;

public interface FileRepository extends JpaRepository<CloudFile, Long> {

    List<CloudFile> findAllByFileOwnerId(Long fileOwner);
}
