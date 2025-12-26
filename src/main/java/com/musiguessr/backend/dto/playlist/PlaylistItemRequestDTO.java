package com.musiguessr.backend.dto.playlist;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaylistItemRequestDTO {

    @NotNull
    private Long songId;

    @NotNull
    private Integer position;
}
