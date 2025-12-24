package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.UserResponseDTO;
import com.musiguessr.backend.dto.MeProfileDTO;
import com.musiguessr.backend.dto.GameHistoryDTO;
import com.musiguessr.backend.dto.TournamentHistoryDTO;
import com.musiguessr.backend.util.AuthUtil;
import com.musiguessr.backend.service.UserService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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
    public ResponseEntity<MeProfileDTO> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = AuthUtil.requireUserId(userDetails, userService.getUserRepository());
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @GetMapping("/me/history/games")
    public ResponseEntity<List<GameHistoryDTO>> getGameHistory(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = AuthUtil.requireUserId(userDetails, userService.getUserRepository());
        return ResponseEntity.ok(userService.getGameHistory(userId));
    }

    @GetMapping("/me/history/tournaments")
    public ResponseEntity<List<TournamentHistoryDTO>> getTournamentHistory(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = AuthUtil.requireUserId(userDetails, userService.getUserRepository());
        return ResponseEntity.ok(userService.getTournamentHistory(userId));
    }
}
