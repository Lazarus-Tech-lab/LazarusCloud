package ru.red.lazaruscloud.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.red.lazaruscloud.dto.UserDataDto;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.service.CloudQuotaService;
import ru.red.lazaruscloud.service.UserStorageService;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserController {

    private final UserStorageService userStorageService;

    @GetMapping("/userdata")
    public ResponseEntity<UserDataDto> getUserData(@AuthenticationPrincipal LazarusUserDetail userDetail) {
        return new ResponseEntity<>(userStorageService.getUserData(userDetail), HttpStatus.OK);
    }
}
