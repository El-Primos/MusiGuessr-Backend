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
    private Long owner_id;
    private Boolean is_curated;
    private OffsetDateTime created_at;

    public PlaylistResponseDTO(Long id, String name, Long owner_id, Boolean is_curated, OffsetDateTime created_at) {
        this.message = null;
        this.id = id;
        this.name = name;
        this.owner_id = owner_id;
        this.is_curated = is_curated;
        this.created_at = created_at;
    }
}
