package com.musiguessr.backend.dto.game;

import lombok.Data;

@Data
public class GameResultDTO {
    private Long id;
    private Integer finalScore;
    private Long historyId;
}
