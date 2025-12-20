package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.*;
import com.musiguessr.backend.dto.playlist.*;
import com.musiguessr.backend.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping
    public ResponseEntity<List<PlaylistResponseDTO>> getPlaylists(
            @RequestParam(value = "owner_id", required = false) Long ownerId,
            @RequestParam(value = "is_curated", required = false) Boolean isCurated,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset
    ) {
        return ResponseEntity.ok(playlistService.getPlaylists(ownerId, isCurated, q, limit, offset));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponseDTO> getPlaylistById(@PathVariable Long id) {
        return ResponseEntity.ok(playlistService.getPlaylistById(id));
    }

    @PostMapping
    public ResponseEntity<PlaylistResponseDTO> createPlaylist(@Valid @RequestBody PlaylistRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistService.createPlaylist(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PlaylistResponseDTO> updatePlaylist(@PathVariable Long id,
                                                              @RequestBody PlaylistUpdateRequestDTO request) {
        return ResponseEntity.ok(playlistService.updatePlaylist(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/songs")
    public ResponseEntity<List<MusicResponseDTO>> getPlaylistSongs(@PathVariable Long id) {
        return ResponseEntity.ok(playlistService.getPlaylistSongs(id));
    }

    @PostMapping("/{id}/songs")
    public ResponseEntity<Void> addSongToPlaylist(@PathVariable Long id,
                                                  @Valid @RequestBody PlaylistAddSongRequestDTO request) {
        playlistService.addSongToPlaylist(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/songs/{song_id}")
    public ResponseEntity<Void> removeSongFromPlaylist(@PathVariable Long id,
                                                       @PathVariable("song_id") Long songId) {
        playlistService.removeSongFromPlaylist(id, songId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reorder")
    public ResponseEntity<Void> reorder(@PathVariable Long id,
                                        @Valid @RequestBody PlaylistReorderRequestDTO request) {
        playlistService.reorder(id, request);
        return ResponseEntity.ok().build();
    }
}
