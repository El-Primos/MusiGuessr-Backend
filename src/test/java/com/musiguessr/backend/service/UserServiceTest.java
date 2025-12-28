package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.UserResponseDTO;
import com.musiguessr.backend.dto.user.ProfilePicturePresignResponseDTO;
import com.musiguessr.backend.dto.user.UserUpdateRequestDTO;
import com.musiguessr.backend.model.User;
import com.musiguessr.backend.model.UserRole;
import com.musiguessr.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getUser_ShouldReturnUser_WhenExists() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setRole(UserRole.USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO result = userService.getUser(1L);

        assertNotNull(result);
        assertEquals("test", result.getUsername());
    }

    @Test
    void getUser_ShouldThrow_WhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> userService.getUser(1L));
    }

    @Test
    void updateUser_ShouldUpdateName() {
        User user = new User();
        user.setId(1L);
        user.setName("Old Name");
        user.setRole(UserRole.USER);

        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setName("New Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO result = userService.updateUser(1L, request);

        assertEquals("New Name", result.getName());
    }

    @Test
    void updatePassword_ShouldUpdate_WhenCurrentPasswordCorrect() {
        User user = new User();
        user.setId(1L);
        user.setPassword("encodedOldPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        userService.updatePassword(1L, "oldPass", "newPass");

        verify(userRepository).save(user);
        assertEquals("encodedNewPass", user.getPassword());
    }

    @Test
    void updatePassword_ShouldThrow_WhenCurrentPasswordIncorrect() {
        User user = new User();
        user.setId(1L);
        user.setPassword("encodedOldPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encodedOldPass")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () ->
                userService.updatePassword(1L, "wrongPass", "newPass")
        );
    }

    @Test
    void presignProfilePicture_ShouldReturnUrl_WhenValid() {
        Long userId = 1L;
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        String presignedUrl = "http://presigned-url";

        when(s3Service.createPresignedUploadUrl(anyString(), eq(contentType))).thenReturn(presignedUrl);

        ProfilePicturePresignResponseDTO result = userService.presignProfilePicture(userId, fileName, contentType);

        assertNotNull(result);
        assertEquals(presignedUrl, result.getUploadUrl());
        assertTrue(result.getKey().startsWith("profile-pictures/1/"));
    }

    @Test
    void presignProfilePicture_ShouldThrow_WhenInvalidExtension() {
        assertThrows(ResponseStatusException.class, () ->
                userService.presignProfilePicture(1L, "test.txt", "text/plain")
        );
    }

    @Test
    void presignProfilePicture_ShouldThrow_WhenMimeTypeMismatch() {
        assertThrows(ResponseStatusException.class, () ->
                userService.presignProfilePicture(1L, "test.jpg", "image/png")
        );
    }

    @Test
    void confirmProfilePicture_ShouldUpdateUrl_WhenFileExists() {
        Long userId = 1L;
        String key = "profile-pictures/1/new.jpg";
        String publicUrl = "http://s3.com/" + key;

        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.USER);
        user.setProfilePictureUrl("http://s3.com/profile-pictures/1/old.jpg");

        when(s3Service.doesFileExist(key)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(s3Service.getUrl(key)).thenReturn(publicUrl);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponseDTO result = userService.confirmProfilePicture(userId, key);

        assertEquals(publicUrl, result.getProfilePictureUrl());
        // Verify old file deletion logic
        verify(s3Service).deleteFile("profile-pictures/1/old.jpg");
    }

    @Test
    void confirmProfilePicture_ShouldThrow_WhenFileDoesNotExist() {
        when(s3Service.doesFileExist("invalid-key")).thenReturn(false);
        assertThrows(ResponseStatusException.class, () ->
                userService.confirmProfilePicture(1L, "invalid-key")
        );
    }
}