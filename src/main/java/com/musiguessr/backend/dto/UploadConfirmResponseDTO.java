package com.musiguessr.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadConfirmResponseDTO {
    private String message;
    private Long id;
    private String name;
    private String url;
}
