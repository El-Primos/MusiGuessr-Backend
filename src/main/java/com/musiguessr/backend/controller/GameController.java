package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.game.*;
import com.musiguessr.backend.service.GameService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/api/games")
    public ResponseEntity<GameDTO> createGame(
            @RequestParam("user_id") Long userId,
            @Valid @RequestBody GameCreateRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gameService.createGame(userId, request));
    }

    @PostMapping("/api/games/{id}/start")
    public ResponseEntity<GameDTO> startGame(
            @RequestParam("user_id") Long userId,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(gameService.startGame(userId, id));
    }

    @GetMapping("/api/games/{id}")
    public ResponseEntity<GameDTO> getGame(
            @RequestParam("user_id") Long userId,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(gameService.getGame(userId, id));
    }

    @GetMapping("/api/games/{id}/next")
    public ResponseEntity<GameNextDTO> next(
            @RequestParam("user_id") Long userId,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(gameService.next(userId, id));
    }

    @PostMapping("/api/games/{id}/guess")
    public ResponseEntity<GameGuessDTO.Response> guess(
            @RequestParam("user_id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody GameGuessDTO.Request request
    ) {
        return ResponseEntity.ok(gameService.guess(userId, id, request));
    }

    @PostMapping("/api/games/{id}/finish")
    public ResponseEntity<GameDTO> finish(
            @RequestParam("user_id") Long userId,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(gameService.finish(userId, id));
    }

    @GetMapping("/api/games")
    public ResponseEntity<List<GameDTO>> getGames(
            @RequestParam("user_id") Long userId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset
    ) {
        return ResponseEntity.ok(
                gameService.getGames(userId, userId, status, limit, offset)
        );
    }

    @GetMapping("/api/games/{id}/results")
    public ResponseEntity<List<GameResultDTO>> getGameResults(
            @RequestParam("user_id") Long userId,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(gameService.getGameResults(userId, id));
    }

    @GetMapping("/api/users/{id}/game-results")
    public ResponseEntity<List<GameResultDTO>> getUserGameResults(
            @RequestParam("user_id") Long userId,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(gameService.getUserGameResults(userId, id));
    }
}
