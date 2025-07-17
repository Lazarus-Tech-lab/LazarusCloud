package ru.red.lazaruscloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.red.lazaruscloud.dto.cloudDtos.QuotaDto;
import ru.red.lazaruscloud.model.StorageLimit;
import ru.red.lazaruscloud.model.User;

public interface CloudQuotaRepository extends JpaRepository<StorageLimit, Long> {
    StorageLimit findByUser(User user);
}
