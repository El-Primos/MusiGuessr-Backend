package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.auth.AuthResponseDTO;
import com.musiguessr.backend.dto.auth.LoginRequestDTO;
import com.musiguessr.backend.dto.auth.LogoutRequestDTO;
import com.musiguessr.backend.dto.auth.RegisterRequestDTO;
import com.musiguessr.backend.dto.token.RefreshTokenRequestDTO;
import com.musiguessr.backend.dto.token.RefreshTokenResponseDTO;
import com.musiguessr.backend.service.AuthService;
import com.musiguessr.backend.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(LogoutRequestDTO request) {
        authService.logout(request);
        return ResponseEntity.ok("User logged out");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(refreshTokenService.refreshToken(request));
    }
}
