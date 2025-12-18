package com.musiguessr.backend.dto;

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