package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.music.*;
import com.musiguessr.backend.model.Artist;
import com.musiguessr.backend.model.Genre;
import com.musiguessr.backend.model.Music;
import com.musiguessr.backend.repository.ArtistRepository;
import com.musiguessr.backend.repository.GenreRepository;
import com.musiguessr.backend.repository.MusicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicServiceTest {

    @Mock
    private S3Service s3Service;
    @Mock
    private MusicRepository musicRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private MusicService musicService;

    @Test
    void presign_WhenValidMp3_ShouldReturnUrl() {
        PresignRequestDTO request = new PresignRequestDTO();
        request.setName("Song Name");
        request.setFileName("test.mp3");
        request.setContentType("audio/mpeg");

        when(musicRepository.existsByName("Song Name")).thenReturn(false);
        when(s3Service.createPresignedUploadUrl(anyString(), anyString())).thenReturn("http://s3-url.com");

        PresignResponseDTO response = musicService.presign(request);

        assertNotNull(response.getUploadUrl());
        assertTrue(response.getKey().contains("music/"));
        assertTrue(response.getKey().contains("test.mp3"));
    }

    @Test
    void presign_WhenExtensionAndTypeMismatch_ShouldThrowException() {
        PresignRequestDTO request = new PresignRequestDTO();
        request.setName("Song Name");
        request.setFileName("test.mp3");
        request.setContentType("audio/wav");

        when(musicRepository.existsByName("Song Name")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> musicService.presign(request));

        assertEquals("400 BAD_REQUEST", exception.getStatusCode().toString());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains("Expected 'audio/mpeg'"));
    }

    @Test
    void presign_WhenInvalidExtension_ShouldThrowException() {
        PresignRequestDTO request = new PresignRequestDTO();
        request.setName("Song Name");
        request.setFileName("test.exe");
        request.setContentType("application/octet-stream");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> musicService.presign(request));

        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains("is not supported"));
    }

    @Test
    void confirm_WhenFileExistsInS3_ShouldSaveMusic() {
        UploadConfirmRequestDTO request = new UploadConfirmRequestDTO();
        request.setKey("music/some-uuid_song.mp3");
        request.setName("New Song");
        request.setGenreId(1L);
        request.setArtistId(2L);

        when(s3Service.doesFileExist(request.getKey())).thenReturn(true);
        when(s3Service.getUrl(request.getKey())).thenReturn("http://full-url.com");
        when(genreRepository.findById(1L)).thenReturn(Optional.of(new Genre()));
        when(artistRepository.findById(2L)).thenReturn(Optional.of(new Artist()));

        when(musicRepository.save(any(Music.class))).thenAnswer(inv -> {
            Music m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });

        UploadConfirmResponseDTO response = musicService.confirm(request);

        assertEquals(10L, response.getId());
        assertEquals("http://full-url.com", response.getUrl());
        verify(musicRepository).save(any(Music.class));
    }

    @Test
    void getMusics_shouldReturnFilteredArtists() {
        Music music = new Music();
        music.setId(1L);
        music.setName("Bohemian Rhapsody");
        when(musicRepository.findAll()).thenReturn(List.of(music));

        List<MusicResponseDTO> result =
                musicService.getMusics("bohem", null, null, 10, 0);

        assertEquals(1, result.size());
        assertEquals("Bohemian Rhapsody", result.getFirst().getName());
    }

    @Test
    void getMusicById_WhenExists_ShouldReturnGenre() {
        Music music = new Music();
        music.setId(1L);
        music.setName("Bohemian Rhapsody");

        when(musicRepository.findById(1L)).thenReturn(Optional.of(music));

        MusicResponseDTO result = musicService.getMusicById(1L);

        assertNotNull(result);
        assertEquals("Bohemian Rhapsody", result.getName());
    }

    @Test
    void updateMusic_WhenValid_ShouldUpdate() {
        Music existingMusic = new Music();
        existingMusic.setId(1L);
        existingMusic.setName("Old Name");

        MusicRequestDTO request = new MusicRequestDTO();
        request.setName("New Name");

        when(musicRepository.findById(1L)).thenReturn(Optional.of(existingMusic));
        when(musicRepository.existsByName("New Name")).thenReturn(false);
        when(musicRepository.save(any(Music.class))).thenAnswer(i -> i.getArgument(0));

        MusicResponseDTO result = musicService.updateMusic(1L, request);

        assertEquals("New Name", result.getName());
    }

    @Test
    void updateMusic_WhenNameConflict_ShouldThrowException() {
        Music existingMusic = new Music();
        existingMusic.setId(1L);
        existingMusic.setName("Bohemian Rhapsody");

        MusicRequestDTO request = new MusicRequestDTO();
        request.setName("Stairway to Heaven");

        when(musicRepository.findById(1L)).thenReturn(Optional.of(existingMusic));
        when(musicRepository.existsByName("Stairway to Heaven")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> musicService.updateMusic(1L, request));

        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains("Music name already exists"));
    }

    @Test
    void deleteMusic_ShouldDeleteFromS3AndDB() {
        Music music = new Music();
        music.setId(1L);
        music.setUrl("https://bucket.s3.region.amazonaws.com/music/file.mp3");

        when(musicRepository.findById(1L)).thenReturn(Optional.of(music));

        musicService.deleteMusic(1L);

        verify(s3Service).deleteFile("music/file.mp3");
        verify(musicRepository).delete(music);
    }
}