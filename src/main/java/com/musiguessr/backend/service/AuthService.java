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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);

        User saved = userRepository.save(user);
        String token = jwtUtil.generateJwtToken(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(saved.getId());

        return new AuthResponseDTO(
                "User registered",
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getRole(),
                token,
                refreshToken.getToken()
        );
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (DisabledException e) {

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
            }

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your account is disabled. Please contact support.");

        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.user();

        String token = jwtUtil.generateJwtToken(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user.getId());

        return new AuthResponseDTO(
                "User logged in",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                token,
                refreshToken.getToken()
        );
    }

    @Transactional
    public void logout(LogoutRequestDTO request) {
        refreshTokenRepository.deleteByToken(request.getRefreshToken());
    }
}
