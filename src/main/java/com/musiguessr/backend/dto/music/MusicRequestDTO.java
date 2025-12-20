package com.musiguessr.backend.dto.music;

import lombok.Data;

@Data
public class MusicRequestDTO {
    private String name;
    private Long genre_id;
    private Long artist_id;
}