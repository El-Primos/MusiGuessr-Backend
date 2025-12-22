package com.musiguessr.backend.dto.playlist;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaylistAddSongRequestDTO {
    @NotNull
    private Long songId;

    private Integer position;
}