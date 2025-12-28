package com.musiguessr.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiguessr.backend.dto.UserResponseDTO;
import com.musiguessr.backend.dto.user.*;
import com.musiguessr.backend.model.UserRole;
import com.musiguessr.backend.security.JwtUtil;
import com.musiguessr.backend.service.UserService;
import com.musiguessr.backend.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private final Long CURRENT_USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthUtil authUtil;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponseDTO mockUserDto;

    @BeforeEach
    void setUp() {
        mockUserDto = new UserResponseDTO(CURRENT_USER_ID, "Test User", "testuser", "test@mail.com", 100, "USER", "http://url");
        when(authUtil.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        when(userService.getUser(CURRENT_USER_ID)).thenReturn(mockUserDto);

        mockMvc.perform(get("/api/users/{id}", CURRENT_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void listUsers_ShouldReturnList() throws Exception {
        when(userService.listUsers()).thenReturn(List.of(mockUserDto));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    void updateCurrentUser_ShouldReturnUpdatedUser() throws Exception {
        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setName("New Name");

        when(userService.updateUser(eq(CURRENT_USER_ID), any(UserUpdateRequestDTO.class))).thenReturn(mockUserDto);

        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePassword_ShouldReturnSuccess() throws Exception {
        PasswordUpdateRequestDTO request = new PasswordUpdateRequestDTO();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass");

        mockMvc.perform(patch("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully"));

        verify(userService).updatePassword(CURRENT_USER_ID, "oldPass", "newPass");
    }

    @Test
    void presignProfilePicture_ShouldReturnPresignData() throws Exception {
        ProfilePicturePresignRequestDTO request = new ProfilePicturePresignRequestDTO();
        request.setFileName("test.jpg");
        request.setContentType("image/jpeg");

        ProfilePicturePresignResponseDTO response = new ProfilePicturePresignResponseDTO(
                "message", "key", "http://upload-url");

        when(userService.presignProfilePicture(CURRENT_USER_ID, "test.jpg", "image/jpeg")).thenReturn(response);

        mockMvc.perform(post("/api/users/me/profile-picture/presign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").value("http://upload-url"));
    }

    @Test
    void confirmProfilePicture_ShouldReturnUser() throws Exception {
        ProfilePictureConfirmRequestDTO request = new ProfilePictureConfirmRequestDTO();
        request.setKey("some-key");

        when(userService.confirmProfilePicture(CURRENT_USER_ID, "some-key")).thenReturn(mockUserDto);

        mockMvc.perform(post("/api/users/me/profile-picture/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePictureUrl").value("http://url"));
    }

    @Test
    void deleteUser_ShouldReturnSuccess() throws Exception {
        Long userIdToDelete = 2L;
        mockMvc.perform(delete("/api/users/{id}", userIdToDelete))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User with id 2 deleted successfully"));

        verify(userService).deleteUser(userIdToDelete);
    }

    @Test
    void updateUserRole_ShouldReturnUpdatedUser() throws Exception {
        Long userIdToUpdate = 2L;
        UserRoleUpdateRequestDTO request = new UserRoleUpdateRequestDTO();
        request.setRole(UserRole.ADMIN);

        UserResponseDTO adminUser = new UserResponseDTO(userIdToUpdate, "Admin", "admin", "admin@mail.com", 0, "ADMIN", null);

        when(userService.updateUserRole(userIdToUpdate, UserRole.ADMIN)).thenReturn(adminUser);

        mockMvc.perform(patch("/api/users/{id}/role", userIdToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}