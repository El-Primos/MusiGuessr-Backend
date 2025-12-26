package com.musiguessr.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfilePictureConfirmRequestDTO {

    @NotBlank
    private String key;
}
