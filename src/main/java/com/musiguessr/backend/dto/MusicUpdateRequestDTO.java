package com.musiguessr.backend.dto;

import lombok.Data;

@Data
public class MusicUpdateRequestDTO {
    private String name;
    private Long genre_id;
    private Long artist_id;
}