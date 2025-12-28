package com.musiguessr.backend.util;

import com.musiguessr.backend.model.User;
import com.musiguessr.backend.model.UserRole;
import com.musiguessr.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUtilTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthUtil authUtil;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_ShouldReturnUser_WhenAuthenticatedAndFound() {
        String username = "testuser";
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setRole(UserRole.USER);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = authUtil.getCurrentUser();

        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void getCurrentUser_ShouldReturnUser_WhenPrincipalIsString() {
        String username = "stringUser";
        User user = new User();
        user.setUsername(username);
        user.setRole(UserRole.USER);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = authUtil.getCurrentUser();

        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void getCurrentUser_ShouldThrowUnauthorized_WhenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authUtil.getCurrentUser()
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("User is not authenticated", exception.getReason());
    }

    @Test
    void getCurrentUser_ShouldThrowUnauthorized_WhenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authUtil.getCurrentUser()
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void getCurrentUser_ShouldThrowUnauthorized_WhenAnonymousUser() {
        Authentication anonymousAuth = mock(AnonymousAuthenticationToken.class);

        when(securityContext.getAuthentication()).thenReturn(anonymousAuth);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authUtil.getCurrentUser()
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void getCurrentUser_ShouldThrowNotFound_WhenUserNotInDb() {
        String username = "ghost";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authUtil.getCurrentUser()
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getCurrentUser_ShouldThrowUnauthorized_WhenUserIsBanned() {
        String username = "bannedUser";
        User bannedUser = new User();
        bannedUser.setUsername(username);
        bannedUser.setRole(UserRole.BANNED);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(bannedUser));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authUtil.getCurrentUser()
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains("account is disabled"));
    }

    @Test
    void getCurrentUserId_ShouldReturnId() {
        String username = "user1";
        User user = new User();
        user.setId(100L);
        user.setRole(UserRole.USER);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Long userId = authUtil.getCurrentUserId();

        assertEquals(100L, userId);
    }
}