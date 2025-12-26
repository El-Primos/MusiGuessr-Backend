package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.artist.ArtistResponseDTO;
import com.musiguessr.backend.dto.genre.GenreResponseDTO;
import com.musiguessr.backend.dto.music.*;
import com.musiguessr.backend.model.Music;
import com.musiguessr.backend.repository.ArtistRepository;
import com.musiguessr.backend.repository.GenreRepository;
import com.musiguessr.backend.repository.MusicRepository;
import com.musiguessr.backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final AuthUtil authUtil;

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
        if (!expectedType.equals(request.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(
                    "Expected '%s' for .%s extension, but got '%s'",
                    expectedType, normalizedExt, request.getContentType()
            ));
        }

        String uniqueKey = "music/" + UUID.randomUUID();
        String uploadUrl = s3Service.createPresignedUploadUrl(uniqueKey, request.getContentType());

        return new PresignResponseDTO("Presign upload url created", uniqueKey, uploadUrl);
    }

    @Transactional
    public UploadConfirmResponseDTO confirm(UploadConfirmRequestDTO request) {
        if (!s3Service.doesFileExist(request.getKey())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in S3");
        }

        String url = s3Service.getUrl(request.getKey());

        // Validate that genre and artist exist
        if (!genreRepository.existsById(request.getGenreId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Genre not found");
        }
        if (!artistRepository.existsById(request.getArtistId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found");
        }

        Music music = new Music();
        music.setOwnerId(authUtil.getCurrentUserId());
        music.setName(request.getName());
        music.setUrl(url);
        music.setGenreId(request.getGenreId());
        music.setArtistId(request.getArtistId());

        Music savedMusic = musicRepository.save(music);

        return new UploadConfirmResponseDTO("Music uploaded", savedMusic.getId(), savedMusic.getName(),
                savedMusic.getUrl());
    }

    @Transactional(readOnly = true)
    public List<MusicResponseDTO> getMusics(String name, Long artistId, Long genreId, Integer limit, Integer offset) {
        Stream<Music> stream = musicRepository.findAll().stream();

        if (name != null) stream = stream.filter(p -> p.getName().toLowerCase().startsWith(name.toLowerCase()));
        if (artistId != null) stream = stream.filter(p -> Objects.equals(p.getArtistId(), artistId));
        if (genreId != null) stream = stream.filter(p -> Objects.equals(p.getGenreId(), genreId));

        int safeOffset = (offset == null || offset < 0) ? 0 : offset;
        int safeLimit = (limit == null || limit < 0) ? 50 : limit;

        return stream.skip(safeOffset).limit(safeLimit)
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
    public MusicResponseDTO updateMusic(Long id, MusicRequestDTO request) {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Music not found"));

        if (StringUtils.hasText(request.getName())) {
            if (!music.getName().equalsIgnoreCase(request.getName()) && musicRepository.existsByName(request.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Music name already exists");
            }
            music.setName(request.getName());
        }

        if (request.getGenreId() != null) {
            // Validate that genre exists
            if (!genreRepository.existsById(request.getGenreId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Genre not found");
            }
            music.setGenreId(request.getGenreId());
        }

        if (request.getArtistId() != null) {
            // Validate that artist exists
            if (!artistRepository.existsById(request.getArtistId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found");
            }
            music.setArtistId(request.getArtistId());
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
                ? new GenreResponseDTO(music.getGenreId(), music.getGenre().getName())
                : null;

        ArtistResponseDTO artistDTO = (music.getArtist() != null)
                ? new ArtistResponseDTO(music.getArtistId(), music.getArtist().getName())
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