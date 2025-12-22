package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.tournament.*;
import com.musiguessr.backend.model.TournamentState;
import com.musiguessr.backend.model.User;
import com.musiguessr.backend.repository.UserRepository;
import com.musiguessr.backend.service.TournamentService;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<TournamentResponseDTO>> getTournaments(
            @RequestParam(value = "status", required = false) TournamentState status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "createDate") String sortBy,
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
    public ResponseEntity<TournamentResponseDTO> createTournament(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TournamentCreateRequestDTO request
    ) {
        Long userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tournamentService.createTournament(userId, request));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<TournamentResponseDTO> joinTournament(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(tournamentService.joinTournament(userId, id));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TournamentResponseDTO> startTournament(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.startTournament(id));
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TournamentResponseDTO> endTournament(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.endTournament(id));
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<TournamentParticipantDTO>> getParticipants(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getParticipants(id));
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<TournamentLeaderboardEntryDTO>> getLeaderboard(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getLeaderboard(id));
    }

    // ---------------- helper ----------------

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getId();
    }
}
