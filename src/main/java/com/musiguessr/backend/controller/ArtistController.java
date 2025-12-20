package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.artist.ArtistRequestDTO;
import com.musiguessr.backend.dto.artist.ArtistResponseDTO;
import com.musiguessr.backend.service.ArtistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping
    public ResponseEntity<List<ArtistResponseDTO>> getArtists(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset) {
        return ResponseEntity.ok(artistService.getArtists(name, limit, offset));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponseDTO> getArtistById(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.getArtistById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtistResponseDTO> createArtist(@Valid @RequestBody ArtistRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(artistService.createArtist(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtistResponseDTO> updateArtist(
            @PathVariable Long id,
            @Valid @RequestBody ArtistRequestDTO request) {
        return ResponseEntity.ok(artistService.updateArtist(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }
}