package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.game.*;
import com.musiguessr.backend.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/api/games")
    public ResponseEntity<GameResponseDTO> createGame() {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.createGame());
    }

    @PostMapping("/api/games/tournament")
    public ResponseEntity<GameResponseDTO> createTournamentGame(
            @RequestParam Long tournamentId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.createTournamentGame(tournamentId));
    }

    @PostMapping("/api/games/{id}/start")
    public ResponseEntity<GameStartDTO> startGame(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.startGame(id));
    }

    @GetMapping("/api/games/{id}/skip")
    public ResponseEntity<GameRoundResultDTO> skip(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.skip(id));
    }

    @PostMapping("/api/games/{id}/guess")
    public ResponseEntity<GameRoundResultDTO> guess(@PathVariable Long id,
                                                    @Valid @RequestBody GameRoundGuessDTO request) {
        return ResponseEntity.ok(gameService.guess(id, request));
    }

    @PostMapping("/api/games/{id}/finish")
    public ResponseEntity<GameResultDTO> finish(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.finish(id));
    }
}
