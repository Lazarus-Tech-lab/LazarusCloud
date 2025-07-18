package ru.red.lazaruscloud.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.red.lazaruscloud.dto.cloudDtos.CloudFolderDto;
import ru.red.lazaruscloud.dto.cloudDtos.UserDataDto;
import ru.red.lazaruscloud.model.CloudFile;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.model.StorageLimit;
import ru.red.lazaruscloud.model.User;

@Service
@AllArgsConstructor
public class UserStorageService {

    private final CloudFileService cloudFileService;
    private final CloudQuotaService cloudQuotaService;
    public void createRootUserFolder(User user, CloudFolderDto cloudFolderDto) {
        cloudFileService.createPhysicalFolder(user,cloudFolderDto);
        cloudQuotaService.createUserQuota(user);
    }

    public CloudFile createUserFolder(LazarusUserDetail userDetail, CloudFolderDto cloudFolderDto) {
        return cloudFileService.createVirtualFolder(userDetail, cloudFolderDto);
    }

    public UserDataDto getUserData(LazarusUserDetail userDetail) {


        User u = new User();


        u.setId(userDetail.getId());


        StorageLimit st = cloudQuotaService.getQuota(u);


        return new UserDataDto(userDetail.getId(), userDetail.getUsername(), st.getQuotaLimit(), st.getQuotaUsedLimit());


    }
}
