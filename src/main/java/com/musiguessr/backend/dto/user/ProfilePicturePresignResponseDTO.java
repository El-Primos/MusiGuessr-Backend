package com.musiguessr.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfilePicturePresignResponseDTO {

    private String message;
    private String key;
    private String uploadUrl;
}
