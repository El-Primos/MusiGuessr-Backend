package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.token.RefreshTokenRequestDTO;
import com.musiguessr.backend.dto.token.RefreshTokenResponseDTO;
import com.musiguessr.backend.model.RefreshToken;
import com.musiguessr.backend.model.User;
import com.musiguessr.backend.repository.RefreshTokenRepository;
import com.musiguessr.backend.repository.UserRepository;
import com.musiguessr.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refreshExpiration}")
    private Long refreshTokenDurationMs;

    @Transactional
    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token not found"));

        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token was expired");
        }

        User user = refreshToken.getUser();
        refreshTokenRepository.delete(refreshToken);

        String token = jwtUtil.generateJwtToken(user.getUsername());
        RefreshToken newRefreshToken = generateRefreshToken(user.getId());

        return new RefreshTokenResponseDTO(token, newRefreshToken.getToken());
    }

    @Transactional
    public RefreshToken generateRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        userRepository.findById(userId).ifPresent(refreshToken::setUser);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }
}