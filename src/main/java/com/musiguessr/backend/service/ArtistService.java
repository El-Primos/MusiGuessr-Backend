package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.artist.ArtistRequestDTO;
import com.musiguessr.backend.dto.artist.ArtistResponseDTO;
import com.musiguessr.backend.model.Artist;
import com.musiguessr.backend.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;

    @Transactional(readOnly = true)
    public List<ArtistResponseDTO> getArtists(String name, Integer limit, Integer offset) {
        Stream<Artist> stream = artistRepository.findAll().stream();

        if (name != null) stream = stream.filter(p -> p.getName().toLowerCase().startsWith(name.toLowerCase()));

        int safeOffset = (offset == null || offset < 0) ? 0 : offset;
        int safeLimit = (limit == null || limit < 0) ? 50 : limit;

        return stream.skip(safeOffset).limit(safeLimit)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ArtistResponseDTO getArtistById(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found"));

        return mapToDTO(artist);
    }

    @Transactional
    public ArtistResponseDTO createArtist(ArtistRequestDTO request) {
        if (artistRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Artist already exists");
        }

        Artist artist = new Artist();
        artist.setName(request.getName());

        Artist savedArtist = artistRepository.save(artist);

        return mapToDTO("Artist created", savedArtist);
    }

    @Transactional
    public ArtistResponseDTO updateArtist(Long id, ArtistRequestDTO request) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            if (artistRepository.existsByName(request.getName()) &&
                    !artist.getName().equalsIgnoreCase(request.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Artist name already exists");
            }

            artist.setName(request.getName());
        }

        Artist updatedArtist = artistRepository.save(artist);

        return mapToDTO("Artist updated", updatedArtist);
    }

    @Transactional
    public void deleteArtist(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Music not found"));

        artistRepository.delete(artist);
    }

    private ArtistResponseDTO mapToDTO(Artist artist) {
        return new ArtistResponseDTO(artist.getId(), artist.getName());
    }

    private ArtistResponseDTO mapToDTO(String message, Artist artist) {
        return new ArtistResponseDTO(message, artist.getId(), artist.getName());
    }
}