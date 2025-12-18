package com.musiguessr.backend.dto.playlist;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlaylistRequestDTO {

    @NotBlank
    private String name;

    private Long owner_id;

    private Boolean is_curated;
}
