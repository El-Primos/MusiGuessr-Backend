package com.musiguessr.backend.dto.game;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {
    private Long id;
    private String status;
    private String type;
    private Long playlistId;

    private Integer totalScore;
    private OffsetDateTime startedAt;
    private OffsetDateTime playedAt;
}
