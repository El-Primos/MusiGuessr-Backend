package com.musiguessr.backend.dto.music;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadConfirmRequestDTO {

    @NotBlank
    private String name;

    @NotNull
    private Long genreId;

    @NotNull
    private Long artistId;

    @NotBlank
    private String key;
}
