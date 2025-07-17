package ru.red.lazaruscloud.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.model.User;

@Service
@AllArgsConstructor
public class UserStorageService {

    private final CloudFileService cloudFileService;
    public void createRootUserFolder(User user, String folderName) {
        cloudFileService.createFolder(user,folderName+"Root", "/");
    }
}
