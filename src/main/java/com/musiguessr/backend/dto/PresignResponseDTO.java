package com.musiguessr.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresignResponseDTO {
    private String message;
    private String key;
    private String uploadUrl;
}
