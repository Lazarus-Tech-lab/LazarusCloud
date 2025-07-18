package ru.red.lazaruscloud.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.red.lazaruscloud.dto.cloudDtos.CloudFolderDto;
import ru.red.lazaruscloud.dto.cloudDtos.QuotaDto;
import ru.red.lazaruscloud.model.StorageLimit;
import ru.red.lazaruscloud.model.User;
import ru.red.lazaruscloud.repository.CloudQuotaRepository;

@Service
@AllArgsConstructor
public class CloudQuotaService {

    private final CloudQuotaRepository cloudQuotaRepository;

    public void createUserQuota(User user) {
        StorageLimit storageLimit = new StorageLimit();
        storageLimit.setQuotaLimit(53687091200L);
        storageLimit.setUser(user);
        cloudQuotaRepository.save(storageLimit);
    }

    public StorageLimit getQuota(User user) {
        return cloudQuotaRepository.findByUser(user);
    }

    public void setUsedQuota(StorageLimit storageLimit) {
        cloudQuotaRepository.save(storageLimit);
    }


}
