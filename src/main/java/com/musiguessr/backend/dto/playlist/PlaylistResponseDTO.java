package com.musiguessr.backend.dto.playlist;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlaylistResponseDTO {
    private String message;
    private Long id;
    private String name;
    private Long ownerId;
    private OffsetDateTime createdAt;

    public PlaylistResponseDTO(Long id, String name, Long ownerId, OffsetDateTime createdAt) {
        this.message = null;
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }
}