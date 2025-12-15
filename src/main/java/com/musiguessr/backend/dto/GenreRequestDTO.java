package com.musiguessr.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenreRequestDTO {
    @NotBlank
    private String name;
}