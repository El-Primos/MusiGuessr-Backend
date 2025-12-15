package com.musiguessr.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PresignRequestDTO {
    @NotBlank
    private String name;

    @NotBlank
    private String fileName;

    @NotBlank
    private String contentType;
}
