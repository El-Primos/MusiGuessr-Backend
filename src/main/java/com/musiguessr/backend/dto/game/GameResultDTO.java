package com.musiguessr.backend.dto.game;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameResultDTO {
    private Long gameId;
    private Long userId;
    private Integer userScore;
    private OffsetDateTime playedAt;
    private Long playlistId;
}
