package com.musiguessr.backend.dto.genre;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenreRequestDTO {
    @NotBlank
    private String name;
}