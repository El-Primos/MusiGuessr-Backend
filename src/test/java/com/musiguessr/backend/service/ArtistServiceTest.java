package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.ArtistRequestDTO;
import com.musiguessr.backend.dto.ArtistResponseDTO;
import com.musiguessr.backend.model.Artist;
import com.musiguessr.backend.repository.ArtistRepository;
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
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private ArtistService artistService;

    @Test
    void getAllArtists_ShouldReturnList() {
        Artist artist = new Artist();
        artist.setId(1L);
        artist.setName("Pink Floyd");
        when(artistRepository.findAll()).thenReturn(List.of(artist));

        List<ArtistResponseDTO> result = artistService.getAllArtists();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Pink Floyd", result.get(0).getName());
    }

    @Test
    void createArtist_WhenNameUnique_ShouldCreate() {
        ArtistRequestDTO request = new ArtistRequestDTO();
        request.setName("Daft Punk");

        when(artistRepository.existsByName("Daft Punk")).thenReturn(false);
        when(artistRepository.save(any(Artist.class))).thenAnswer(invocation -> {
            Artist a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });

        ArtistResponseDTO result = artistService.createArtist(request);

        assertEquals("Daft Punk", result.getName());
        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    void createArtist_WhenNameExists_ShouldThrowException() {
        ArtistRequestDTO request = new ArtistRequestDTO();
        request.setName("Daft Punk");
        when(artistRepository.existsByName("Daft Punk")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> artistService.createArtist(request));
        verify(artistRepository, never()).save(any());
    }

    @Test
    void updateArtist_WhenArtistNotFound_ShouldThrowException() {
        ArtistRequestDTO request = new ArtistRequestDTO();
        when(artistRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> artistService.updateArtist(1L, request));
    }
}