package com.musiguessr.backend.dto.playlist;

import lombok.Data;

@Data
public class PlaylistUpdateRequestDTO {
    private String name;
    private Boolean is_curated;
}
