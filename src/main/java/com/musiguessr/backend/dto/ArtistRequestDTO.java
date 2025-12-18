package com.musiguessr.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArtistRequestDTO {
    @NotBlank
    private String name;
}