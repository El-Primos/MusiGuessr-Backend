package com.musiguessr.backend.dto.artist;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArtistRequestDTO {
    @NotBlank
    private String name;
}