package com.musiguessr.backend.dto.playlist;

import lombok.Data;

@Data
public class PlaylistUpdateRequestDTO {
    private String name;

    // keep for API compatibility; ignored
    private Boolean is_curated;
}
