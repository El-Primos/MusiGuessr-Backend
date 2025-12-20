package com.musiguessr.backend.dto.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameCreateRequestDTO {
    private String type;
    private Long playlistId;
    private Boolean isOffline;
}
