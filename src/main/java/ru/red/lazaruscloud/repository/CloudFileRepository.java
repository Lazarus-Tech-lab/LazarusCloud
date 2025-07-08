package ru.red.lazaruscloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.red.lazaruscloud.model.CloudFile;
import ru.red.lazaruscloud.model.LazarusUserDetail;

import java.util.List;

public interface CloudFileRepository extends JpaRepository<CloudFile, Long> {
    List<CloudFile> findAllByFileOwner(LazarusUserDetail fileOwner);
}
