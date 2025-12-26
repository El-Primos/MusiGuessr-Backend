package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.leaderboard.LeaderboardEntryDTO;
import com.musiguessr.backend.service.LeaderboardService;
import com.musiguessr.backend.util.AuthUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaderboards")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final AuthUtil authUtil;

    @GetMapping("/global")
    public ResponseEntity<List<LeaderboardEntryDTO>> getGlobalLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getGlobalLeaderboard());
    }

    @GetMapping("/playlist/{playlistId}")
    public ResponseEntity<List<LeaderboardEntryDTO>> getPlaylistLeaderboard(
            @PathVariable Long playlistId
    ) {
        return ResponseEntity.ok(leaderboardService.getPlaylistLeaderboard(playlistId));
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<LeaderboardEntryDTO>> getTournamentLeaderboard(
            @PathVariable Long tournamentId
    ) {
        return ResponseEntity.ok(leaderboardService.getTournamentLeaderboard(tournamentId));
    }

    @GetMapping("/friends")
    public ResponseEntity<List<LeaderboardEntryDTO>> getFriendsLeaderboard() {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(leaderboardService.getFriendsLeaderboard(userId));
    }
}
