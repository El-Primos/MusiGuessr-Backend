package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.token.RefreshTokenRequestDTO;
import com.musiguessr.backend.dto.token.RefreshTokenResponseDTO;
import com.musiguessr.backend.model.RefreshToken;
import com.musiguessr.backend.model.User;
import com.musiguessr.backend.repository.RefreshTokenRepository;
import com.musiguessr.backend.repository.UserRepository;
import com.musiguessr.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User mockUser;
    private RefreshToken mockRefreshToken;

    @BeforeEach
    void setUp() {
        long REFRESH_DURATION_MS = 60000L;
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", REFRESH_DURATION_MS);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        mockRefreshToken = new RefreshToken();
        mockRefreshToken.setId(10L);
        mockRefreshToken.setToken(UUID.randomUUID().toString());
        mockRefreshToken.setUser(mockUser);
        mockRefreshToken.setExpiryDate(Instant.now().plusMillis(REFRESH_DURATION_MS));
    }

    @Test
    void generateRefreshToken_ShouldCreateAndSaveToken_WhenUserExists() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.generateRefreshToken(userId);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(mockUser, result.getUser());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));

        verify(userRepository).findById(userId);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }


    @Test
    void refreshToken_ShouldReturnNewTokens_WhenRequestIsValid() {
        // Arrange
        String oldTokenString = mockRefreshToken.getToken();
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
        request.setRefreshToken(oldTokenString);

        String newJwtToken = "new.jwt.token";

        when(refreshTokenRepository.findByToken(oldTokenString)).thenReturn(Optional.of(mockRefreshToken));

        when(jwtUtil.generateJwtToken(mockUser.getUsername())).thenReturn(newJwtToken);

        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> {
            RefreshToken t = i.getArgument(0);
            t.setToken("new-uuid-token");
            return t;
        });

        RefreshTokenResponseDTO response = refreshTokenService.refreshToken(request);

        assertNotNull(response);
        assertEquals(newJwtToken, response.getAccessToken());
        assertEquals("new-uuid-token", response.getRefreshToken());

        verify(refreshTokenRepository).delete(mockRefreshToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refreshToken_ShouldThrowForbidden_WhenTokenNotFound() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
        request.setRefreshToken("non-existent-token");

        when(refreshTokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                refreshTokenService.refreshToken(request)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Refresh token not found", exception.getReason());

        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void refreshToken_ShouldThrowForbidden_WhenTokenExpired() {
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setUser(mockUser);
        expiredToken.setExpiryDate(Instant.now().minusMillis(1000)); // Geçmiş zaman

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
        request.setRefreshToken("expired-token");

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                refreshTokenService.refreshToken(request)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Refresh token was expired", exception.getReason());

        verify(refreshTokenRepository).delete(expiredToken);
        verify(jwtUtil, never()).generateJwtToken(any());
    }
}