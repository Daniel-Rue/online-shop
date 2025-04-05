package ru.kon.onlineshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kon.onlineshop.dto.auth.JwtResponse;
import ru.kon.onlineshop.dto.auth.LoginRequest;
import ru.kon.onlineshop.dto.auth.RegisterRequest;
import ru.kon.onlineshop.security.auth.JwtUtils;
import ru.kon.onlineshop.service.UserService;

import javax.validation.Valid;
import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String jwt = jwtUtils.generateJwtToken(authentication);
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("error", "Пользователь с такой почтой уже зарегистрирован!"));
        }

        userService.registerUser(request);

        return ResponseEntity.ok(Collections.singletonMap("message", "Пользователь успешно зарегистрирован!"));
    }
}
