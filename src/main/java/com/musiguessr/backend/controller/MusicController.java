package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.music.*;
import com.musiguessr.backend.service.MusicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/musics")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    @PostMapping("/upload-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PresignResponseDTO> getUploadUrl(@Valid @RequestBody PresignRequestDTO request) {
        return ResponseEntity.ok(musicService.presign(request));
    }

    @PostMapping("/upload-confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UploadConfirmResponseDTO> confirmUpload(@Valid @RequestBody UploadConfirmRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(musicService.confirm(request));
    }

    @GetMapping
    public ResponseEntity<List<MusicResponseDTO>> getMusics(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        return ResponseEntity.ok(musicService.getMusics(name, artistId, genreId, limit, offset));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MusicResponseDTO> getMusicById(@PathVariable Long id) {
        return ResponseEntity.ok(musicService.getMusicById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MusicResponseDTO> updateMusic(@PathVariable Long id,
                                                        @RequestBody MusicRequestDTO request) {
        return ResponseEntity.ok(musicService.updateMusic(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMusic(@PathVariable Long id) {
        musicService.deleteMusic(id);
        return ResponseEntity.noContent().build();
    }
}