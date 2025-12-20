package com.musiguessr.backend.dto.music;

import com.musiguessr.backend.dto.artist.ArtistResponseDTO;
import com.musiguessr.backend.dto.genre.GenreResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MusicResponseDTO {
    private Long id;
    private String name;
    private String url;
    private GenreResponseDTO genre;
    private ArtistResponseDTO artist;
}