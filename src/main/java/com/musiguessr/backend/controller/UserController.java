package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.GameHistoryDTO;
import com.musiguessr.backend.dto.MeProfileDTO;
import com.musiguessr.backend.dto.TournamentHistoryDTO;
import com.musiguessr.backend.dto.UserResponseDTO;
import com.musiguessr.backend.dto.user.UserUpdateRequestDTO;
import com.musiguessr.backend.service.UserService;
import com.musiguessr.backend.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthUtil authUtil;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User with id " + id + " deleted successfully"));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody com.musiguessr.backend.dto.user.UserRoleUpdateRequestDTO dto) {
        return ResponseEntity.ok(userService.updateUserRole(id, dto.getRole()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(@Valid @RequestBody UserUpdateRequestDTO dto) {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(userService.updateUser(userId, dto));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<String> updatePassword(
            @Valid @RequestBody com.musiguessr.backend.dto.user.PasswordUpdateRequestDTO dto) {
        Long userId = authUtil.getCurrentUserId();
        userService.updatePassword(userId, dto.getCurrentPassword(), dto.getNewPassword());
        return ResponseEntity.ok("Password updated successfully");
    }

    @PostMapping("/me/profile-picture/presign")
    public ResponseEntity<com.musiguessr.backend.dto.user.ProfilePicturePresignResponseDTO> presignProfilePicture(
            @Valid @RequestBody com.musiguessr.backend.dto.user.ProfilePicturePresignRequestDTO dto) {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(userService.presignProfilePicture(userId, dto.getFileName(), dto.getContentType()));
    }

    @PostMapping("/me/profile-picture/confirm")
    public ResponseEntity<UserResponseDTO> confirmProfilePicture(
            @Valid @RequestBody com.musiguessr.backend.dto.user.ProfilePictureConfirmRequestDTO dto) {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(userService.confirmProfilePicture(userId, dto.getKey()));
    }

    @GetMapping("/me/profile")
    public ResponseEntity<MeProfileDTO> getProfile() {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @GetMapping("/me/history/games")
    public ResponseEntity<List<GameHistoryDTO>> getGameHistory() {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(userService.getGameHistory(userId));
    }

    @GetMapping("/me/history/tournaments")
    public ResponseEntity<List<TournamentHistoryDTO>> getTournamentHistory() {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(userService.getTournamentHistory(userId));
    }
}
