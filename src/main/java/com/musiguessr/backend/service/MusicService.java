package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.artist.ArtistResponseDTO;
import com.musiguessr.backend.dto.genre.GenreResponseDTO;
import com.musiguessr.backend.dto.music.*;
import com.musiguessr.backend.model.Artist;
import com.musiguessr.backend.model.Genre;
import com.musiguessr.backend.model.Music;
import com.musiguessr.backend.repository.ArtistRepository;
import com.musiguessr.backend.repository.GenreRepository;
import com.musiguessr.backend.repository.MusicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MusicService {

    private static final Map<String, String> VALID_FORMATS = new HashMap<>();

    static {
        VALID_FORMATS.put("mp3", "audio/mpeg");
        VALID_FORMATS.put("wav", "audio/wav");
    }

    private final S3Service s3Service;
    private final MusicRepository musicRepository;
    private final GenreRepository genreRepository;
    private final ArtistRepository artistRepository;

    public PresignResponseDTO presign(PresignRequestDTO request) {
        if (musicRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Music already exists");
        }

        String extension = StringUtils.getFilenameExtension(request.getFileName());
        if (extension == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must have an extension");
        }

        String normalizedExt = extension.toLowerCase();
        if (!VALID_FORMATS.containsKey(normalizedExt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Extension '." + normalizedExt + "' is not supported");
        }

        String expectedType = VALID_FORMATS.get(normalizedExt);
        if (!expectedType.equals(request.getContent_type())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(
                    "Error: Mismatch! Expected '%s' for .%s extension, but got '%s'",
                    expectedType, normalizedExt, request.getContent_type()
            ));
        }

        String uniqueKey = "music/" + UUID.randomUUID() + "_" + request.getFileName();
        String uploadUrl = s3Service.createPresignedUploadUrl(uniqueKey, request.getContent_type());

        return new PresignResponseDTO("Presign upload url created", uniqueKey, uploadUrl);
    }

    @Transactional
    public UploadConfirmResponseDTO confirm(UploadConfirmRequestDTO request) {
        if (!s3Service.doesFileExist(request.getKey())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in S3");
        }

        String url = s3Service.getUrl(request.getKey());

        Music music = new Music();
        music.setName(request.getName());
        music.setUrl(url);

        music.setGenre(genreRepository.findById(request.getGenre_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Genre not found")));

        music.setArtist(artistRepository.findById(request.getArtist_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found with ID")));

        Music savedMusic = musicRepository.save(music);

        return new UploadConfirmResponseDTO("Music uploaded", savedMusic.getId(), savedMusic.getName(),
                savedMusic.getUrl());
    }

    @Transactional(readOnly = true)
    public List<MusicResponseDTO> getAllMusic() {
        return musicRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MusicResponseDTO getMusicById(Long id) {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Music not found"));

        return mapToDTO(music);
    }

    @Transactional
    public MusicResponseDTO updateMusic(Long id, MusicUpdateRequestDTO request) {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Music not found"));

        if (StringUtils.hasText(request.getName())) {
            if (!music.getName().equalsIgnoreCase(request.getName()) && musicRepository.existsByName(request.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Music name already exists");
            }
            music.setName(request.getName());
        }

        if (request.getGenre_id() != null) {
            Genre genre = genreRepository.findById(request.getGenre_id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Genre not found with ID: " + request.getGenre_id()));
            music.setGenre(genre);
        }

        if (request.getArtist_id() != null) {
            Artist artist = artistRepository.findById(request.getArtist_id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found"));
            music.setArtist(artist);
        }

        Music updatedMusic = musicRepository.save(music);

        return mapToDTO(updatedMusic);
    }

    @Transactional
    public void deleteMusic(Long id) {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Music not found"));

        String url = music.getUrl();
        if (url != null && url.contains("music/")) {
            String s3Key = url.substring(url.indexOf("music/"));
            s3Service.deleteFile(s3Key);
        }

        musicRepository.delete(music);
    }

    private MusicResponseDTO mapToDTO(Music music) {
        GenreResponseDTO genreDTO = (music.getGenre() != null)
                ? new GenreResponseDTO(music.getGenre().getId(), music.getGenre().getName())
                : null;

        ArtistResponseDTO artistDTO = (music.getArtist() != null)
                ? new ArtistResponseDTO(music.getArtist().getId(), music.getArtist().getName())
                : null;

        return new MusicResponseDTO(
                music.getId(),
                music.getName(),
                music.getUrl(),
                genreDTO,
                artistDTO
        );
    }
}