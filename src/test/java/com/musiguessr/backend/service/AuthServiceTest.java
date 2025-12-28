package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.auth.AuthResponseDTO;
import com.musiguessr.backend.dto.auth.LoginRequestDTO;
import com.musiguessr.backend.dto.auth.LogoutRequestDTO;
import com.musiguessr.backend.dto.auth.RegisterRequestDTO;
import com.musiguessr.backend.model.RefreshToken;
import com.musiguessr.backend.model.User;
import com.musiguessr.backend.model.UserRole;
import com.musiguessr.backend.repository.RefreshTokenRepository;
import com.musiguessr.backend.repository.UserRepository;
import com.musiguessr.backend.security.CustomUserDetails;
import com.musiguessr.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldReturnAuthResponse_WhenUserIsValid() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("Test User");
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");
        savedUser.setRole(UserRole.USER);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-uuid");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateJwtToken(savedUser.getUsername())).thenReturn("jwt-token");
        when(refreshTokenService.generateRefreshToken(savedUser.getId())).thenReturn(refreshToken);

        AuthResponseDTO response = authService.register(request);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("refresh-token-uuid", response.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("existingUser");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("testuser");
        request.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(UserRole.USER);

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.user()).thenReturn(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-uuid");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateJwtToken(user.getUsername())).thenReturn("jwt-token");
        when(refreshTokenService.generateRefreshToken(user.getId())).thenReturn(refreshToken);

        AuthResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("wronguser");
        request.setPassword("wrongpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(ResponseStatusException.class, () -> authService.login(request));
    }

    @Test
    void logout_ShouldDeleteRefreshToken() {
        LogoutRequestDTO request = new LogoutRequestDTO();
        request.setRefreshToken("some-token");

        authService.logout(request);

        verify(refreshTokenRepository, times(1)).deleteByToken("some-token");
    }
}