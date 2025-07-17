package ru.red.lazaruscloud.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.red.lazaruscloud.dto.authDtos.JwtResponseDto;
import ru.red.lazaruscloud.dto.authDtos.LoginRequestDto;
import ru.red.lazaruscloud.dto.authDtos.RegisterRequestDto;
import ru.red.lazaruscloud.dto.cloudDtos.CloudFolderDto;
import ru.red.lazaruscloud.model.CloudStorage;
import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.model.User;
import ru.red.lazaruscloud.security.Role;
import ru.red.lazaruscloud.security.TokenHelper;
import ru.red.lazaruscloud.service.UserDetailService;
import ru.red.lazaruscloud.service.UserService;
import ru.red.lazaruscloud.service.UserStorageService;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailService userDetailService;
    private final TokenHelper tokenHelper;
    private final UserStorageService cloudStorageService;

    @PostMapping("/auth")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LazarusUserDetail userDetails = userDetailService.loadUserByUsername(loginRequestDto.username());

        if(!passwordEncoder.matches(loginRequestDto.password(), userDetails.getPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(tokenHelper.generateTokens(userDetails), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        User user = new User();
        user.setUsername(registerRequestDto.username());
        user.setPassword(passwordEncoder.encode(registerRequestDto.password()));
        user.setEmail(registerRequestDto.email());
        user.setRoles(List.of(Role.ROLE_USER));
        User savedUser = userService.addUser(user);
        CloudFolderDto rootCloudFolder = new CloudFolderDto(0L, savedUser.getUsername()+"Root");
        cloudStorageService.createRootUserFolder(savedUser, rootCloudFolder);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
