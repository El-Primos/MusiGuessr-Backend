package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.leaderboard.LeaderboardEntryDTO;
import com.musiguessr.backend.service.LeaderboardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaderboards")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/global")
    public ResponseEntity<List<LeaderboardEntryDTO>> getGlobalLeaderboard(
            @RequestParam(value = "period", defaultValue = "all") String period,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        return ResponseEntity.ok(leaderboardService.getGlobalLeaderboard(period, limit));
    }

    @GetMapping("/playlist/{playlistId}")
    public ResponseEntity<List<LeaderboardEntryDTO>> getPlaylistLeaderboard(
            @PathVariable Long playlistId,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        return ResponseEntity.ok(leaderboardService.getPlaylistLeaderboard(playlistId, limit));
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<LeaderboardEntryDTO>> getTournamentLeaderboard(
            @PathVariable Long tournamentId
    ) {
        return ResponseEntity.ok(leaderboardService.getTournamentLeaderboard(tournamentId));
    }
}
