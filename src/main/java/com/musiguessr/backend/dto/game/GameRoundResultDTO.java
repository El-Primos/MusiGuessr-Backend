package com.musiguessr.backend.dto.game;

import lombok.Data;

@Data
public class GameRoundResultDTO {
    boolean isGameFinished;
    Integer nextRound;
    String nextPreviewUrl;
    private boolean isCorrect;
    private Integer earnedScore;
    private Integer totalScore;
    private Long correctMusicId;
}
