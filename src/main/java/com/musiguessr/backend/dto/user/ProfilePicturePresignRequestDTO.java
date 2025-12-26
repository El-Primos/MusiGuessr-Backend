package com.musiguessr.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfilePicturePresignRequestDTO {

    @NotBlank
    private String fileName;

    @NotBlank
    private String contentType;
}
