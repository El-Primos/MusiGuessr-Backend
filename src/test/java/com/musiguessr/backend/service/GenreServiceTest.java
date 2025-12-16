package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.GenreRequestDTO;
import com.musiguessr.backend.dto.GenreResponseDTO;
import com.musiguessr.backend.model.Genre;
import com.musiguessr.backend.repository.GenreRepository;
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
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreService genreService;

    @Test
    void getAllGenres_ShouldReturnList() {
        // Arrange
        Genre genre = new Genre();
        genre.setId(1L);
        genre.setName("Rock");

        when(genreRepository.findAll()).thenReturn(List.of(genre));

        List<GenreResponseDTO> result = genreService.getAllGenres();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Rock", result.get(0).getName());
    }

    @Test
    void getGenreById_WhenExists_ShouldReturnGenre() {
        Genre genre = new Genre();
        genre.setId(1L);
        genre.setName("Jazz");

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));

        GenreResponseDTO result = genreService.getGenreById(1L);

        assertNotNull(result);
        assertEquals("Jazz", result.getName());
    }

    @Test
    void getGenreById_WhenNotExists_ShouldThrowException() {
        when(genreRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> genreService.getGenreById(99L));

        assertEquals("404 NOT_FOUND", exception.getStatusCode().toString());
    }

    @Test
    void createGenre_WhenNameUnique_ShouldCreate() {
        GenreRequestDTO request = new GenreRequestDTO();
        request.setName("Pop");

        when(genreRepository.existsByName("Pop")).thenReturn(false);

        when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> {
            Genre g = invocation.getArgument(0);
            g.setId(10L);
            return g;
        });

        GenreResponseDTO result = genreService.createGenre(request);

        assertEquals("Pop", result.getName());
        assertEquals(10L, result.getId());
        verify(genreRepository).save(any(Genre.class));
    }

    @Test
    void createGenre_WhenNameExists_ShouldThrowException() {
        GenreRequestDTO request = new GenreRequestDTO();
        request.setName("Pop");

        when(genreRepository.existsByName("Pop")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> genreService.createGenre(request));
        verify(genreRepository, never()).save(any());
    }

    @Test
    void updateGenre_WhenValid_ShouldUpdate() {
        Genre existingGenre = new Genre();
        existingGenre.setId(1L);
        existingGenre.setName("Old Name");

        GenreRequestDTO request = new GenreRequestDTO();
        request.setName("New Name");

        when(genreRepository.findById(1L)).thenReturn(Optional.of(existingGenre));
        when(genreRepository.existsByName("New Name")).thenReturn(false);
        when(genreRepository.save(any(Genre.class))).thenAnswer(i -> i.getArgument(0));

        GenreResponseDTO result = genreService.updateGenre(1L, request);

        assertEquals("New Name", result.getName());
    }

    @Test
    void updateGenre_WhenNameConflict_ShouldThrowException() {
        Genre existingGenre = new Genre();
        existingGenre.setId(1L);
        existingGenre.setName("Techno");

        GenreRequestDTO request = new GenreRequestDTO();
        request.setName("House");

        when(genreRepository.findById(1L)).thenReturn(Optional.of(existingGenre));
        when(genreRepository.existsByName("House")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> genreService.updateGenre(1L, request));

        assertTrue(exception.getReason().contains("Genre name already exists"));
    }

    @Test
    void deleteGenre_WhenExists_ShouldDelete() {
        Genre genre = new Genre();
        genre.setId(1L);

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));

        genreService.deleteGenre(1L);

        verify(genreRepository).delete(genre);
    }

    @Test
    void deleteGenre_WhenNotExists_ShouldThrowException() {
        when(genreRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> genreService.deleteGenre(1L));
    }
}