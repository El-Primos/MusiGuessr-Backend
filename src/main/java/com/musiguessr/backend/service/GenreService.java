package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.genre.GenreRequestDTO;
import com.musiguessr.backend.dto.genre.GenreResponseDTO;
import com.musiguessr.backend.model.Genre;
import com.musiguessr.backend.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public List<GenreResponseDTO> getGenres(String name, Integer limit, Integer offset) {
        Stream<Genre> stream = genreRepository.findAll().stream();

        if (name != null) {
            String needle = name.trim().toLowerCase();
            stream = stream.filter(p -> p.getName() != null && p.getName().toLowerCase().contains(needle));
        }

        int safeOffset = (offset == null || offset < 0) ? 0 : offset;
        int safeLimit = (limit == null || limit < 0) ? 50 : limit;

        return stream.skip(safeOffset).limit(safeLimit)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GenreResponseDTO getGenreById(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Genre not found"));

        return mapToDTO(genre);
    }

    @Transactional
    public GenreResponseDTO createGenre(GenreRequestDTO request) {
        if (genreRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Genre already exists");
        }

        Genre genre = new Genre();
        genre.setName(request.getName());

        Genre savedGenre = genreRepository.save(genre);

        return mapToDTO("Genre created", savedGenre);
    }

    @Transactional
    public GenreResponseDTO updateGenre(Long id, GenreRequestDTO request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Genre not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            if (genreRepository.existsByName(request.getName()) &&
                    !genre.getName().equalsIgnoreCase(request.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Genre name already exists");
            }

            genre.setName(request.getName());
        }

        Genre updatedGenre = genreRepository.save(genre);

        return mapToDTO("Genre updated", updatedGenre);
    }

    @Transactional
    public void deleteGenre(@PathVariable Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Music not found"));

        genreRepository.delete(genre);
    }

    private GenreResponseDTO mapToDTO(Genre genre) {
        return new GenreResponseDTO(genre.getId(), genre.getName());
    }

    private GenreResponseDTO mapToDTO(String message, Genre genre) {
        return new GenreResponseDTO(message, genre.getId(), genre.getName());
    }
}