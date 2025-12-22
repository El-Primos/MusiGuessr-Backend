package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.music.MusicResponseDTO;
import com.musiguessr.backend.dto.playlist.*;
import com.musiguessr.backend.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping
    public ResponseEntity<List<PlaylistResponseDTO>> getPlaylists(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset
    ) {
        return ResponseEntity.ok(playlistService.getPlaylists(ownerId, name, limit, offset));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponseDTO> getPlaylistById(@PathVariable Long id) {
        return ResponseEntity.ok(playlistService.getPlaylistById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlaylistResponseDTO> createPlaylist(@Valid @RequestBody PlaylistRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistService.createPlaylist(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlaylistResponseDTO> updatePlaylist(@PathVariable Long id,
                                                              @RequestBody PlaylistUpdateRequestDTO request) {
        return ResponseEntity.ok(playlistService.updatePlaylist(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/songs")
    public ResponseEntity<List<MusicResponseDTO>> getPlaylistSongs(@PathVariable Long id) {
        return ResponseEntity.ok(playlistService.getPlaylistSongs(id));
    }

    @PostMapping("/{id}/songs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addSongToPlaylist(@PathVariable Long id,
                                                  @Valid @RequestBody PlaylistAddSongRequestDTO request) {
        playlistService.addSongToPlaylist(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/songs/{songId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeSongFromPlaylist(@PathVariable Long id, @PathVariable Long songId) {
        playlistService.removeSongFromPlaylist(id, songId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reorder(@PathVariable Long id,
                                        @Valid @RequestBody PlaylistReorderRequestDTO request) {
        playlistService.reorder(id, request);
        return ResponseEntity.ok().build();
    }
}
