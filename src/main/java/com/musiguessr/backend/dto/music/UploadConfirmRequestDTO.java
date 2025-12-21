package com.musiguessr.backend.dto.music;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadConfirmRequestDTO {

    @NotBlank
    private String name;

    @NotNull
    private Long genre_id;

    @NotNull
    private Long artist_id;

    @NotBlank
    private String key;
}
