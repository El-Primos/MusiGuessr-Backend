package com.musiguessr.backend.dto.playlist;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlaylistUpdateRequestDTO {

    @NotBlank
    private String name;
}
