package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.GameHistoryDTO;
import com.musiguessr.backend.dto.MeProfileDTO;
import com.musiguessr.backend.dto.TournamentHistoryDTO;
import com.musiguessr.backend.dto.UserResponseDTO;
import com.musiguessr.backend.service.UserService;
import com.musiguessr.backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User with id " + id + " deleted successfully"));
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
