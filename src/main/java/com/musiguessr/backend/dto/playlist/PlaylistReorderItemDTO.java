package com.musiguessr.backend.dto.playlist;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaylistReorderItemDTO {

    @NotNull
    private Long song_id;

    @NotNull
    private Integer position;
}
