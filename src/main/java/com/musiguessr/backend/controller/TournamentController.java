package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.tournament.*;
import com.musiguessr.backend.model.TournamentState;
import com.musiguessr.backend.service.TournamentService;
import com.musiguessr.backend.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;
    private final AuthUtil authUtil;

    @GetMapping
    public ResponseEntity<Page<TournamentResponseDTO>> getTournaments(
            @RequestParam(value = "status", required = false) TournamentState status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(tournamentService.getTournaments(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponseDTO> getTournament(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getTournament(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TournamentResponseDTO> createTournament(@Valid @RequestBody TournamentCreateRequestDTO request) {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tournamentService.createTournament(userId, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TournamentResponseDTO> updateTournament(
            @PathVariable Long id,
            @RequestBody TournamentUpdateRequestDTO request
    ) {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(tournamentService.updateTournament(id, userId, request));
    }

    @PatchMapping("/{id}/state")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TournamentResponseDTO> updateTournamentState(
            @PathVariable Long id,
            @RequestBody TournamentStateUpdateRequestDTO request
    ) {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(tournamentService.updateTournamentState(id, userId, request.getState()));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<TournamentResponseDTO> joinTournament(@PathVariable Long id) {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(tournamentService.joinTournament(userId, id));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<TournamentResponseDTO> leaveTournament(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(tournamentService.leaveTournament(userId, id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTournament(@PathVariable Long id) {
        tournamentService.deleteTournament(id);
        return ResponseEntity.ok("Tournament deleted successfully");
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<TournamentParticipantDTO>> getParticipants(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getParticipants(id));
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<TournamentLeaderboardEntryDTO>> getLeaderboard(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getLeaderboard(id));
    }

}
