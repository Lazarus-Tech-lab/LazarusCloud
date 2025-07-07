package ru.red.lazaruscloud.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.red.lazaruscloud.dto.JwtResponseDto;
import ru.red.lazaruscloud.dto.LoginRequestDto;
import ru.red.lazaruscloud.dto.RegisterRequestDto;
import ru.red.lazaruscloud.model.User;
import ru.red.lazaruscloud.repository.UserRepository;
import ru.red.lazaruscloud.security.Role;
import ru.red.lazaruscloud.security.TokenHelper;
import ru.red.lazaruscloud.service.UserDetailService;
import ru.red.lazaruscloud.service.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailService userDetailService;
    private final TokenHelper tokenHelper;

    @PostMapping("/auth")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        UserDetails userDetails = userDetailService.loadUserByUsername(loginRequestDto.username());

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
        userService.addUser(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
