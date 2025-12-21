package com.musiguessr.backend.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameHistoryDTO {
    private Long gameId;
    private Long playlistId;
    private Integer totalScore;
    private OffsetDateTime playedAt;
}
