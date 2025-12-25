package com.musiguessr.backend.dto.game;

import lombok.Data;

@Data
public class GameStartDTO {
    private Long id;
    private Integer currentRound;
    private Long totalRounds;
    private Integer totalScore;
    private String nextPreviewUrl;
}
