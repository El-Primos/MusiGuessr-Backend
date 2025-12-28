package com.musiguessr.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final String TEST_SECRET = "superSecretKeyForTestingMusiGuessrBackend123!";
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(jwtUtil, "secretKey", TEST_SECRET);
        long TEST_EXPIRATION = 3600000;
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    void generateJwtToken_ShouldReturnNonEmptyToken() {
        String username = "testuser";
        String token = jwtUtil.generateJwtToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String username = "testuser";
        String token = jwtUtil.generateJwtToken(username);

        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtUtil.generateJwtToken("validUser");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsTampered() {
        String validToken = jwtUtil.generateJwtToken("testuser");
        String tamperedToken = validToken.substring(0, validToken.length() - 1) + "X";

        assertFalse(jwtUtil.validateToken(tamperedToken));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
        JwtUtil expiredUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredUtil, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(expiredUtil, "expiration", -1000L);

        String expiredToken = expiredUtil.generateJwtToken("expiredUser");

        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsMalformed() {
        String malformedToken = "this.is.a.invalid.token";
        assertFalse(jwtUtil.validateToken(malformedToken));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenSignedWithDifferentKey() {
        String otherSecret = "differentSecretKeyForTestingMusiGuessrBackend999!";
        SecretKey otherKey = Keys.hmacShaKeyFor(otherSecret.getBytes(StandardCharsets.UTF_8));

        String forgedToken = Jwts.builder()
                .subject("hacker")
                .signWith(otherKey)
                .compact();

        assertFalse(jwtUtil.validateToken(forgedToken));
    }
}