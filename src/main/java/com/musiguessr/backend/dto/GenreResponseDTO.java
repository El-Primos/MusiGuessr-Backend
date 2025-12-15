package com.musiguessr.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenreResponseDTO {
    private String message;
    private Long id;
    private String name;

    public GenreResponseDTO(Long id, String name) {
        this.message = null;
        this.id = id;
        this.name = name;
    }
}