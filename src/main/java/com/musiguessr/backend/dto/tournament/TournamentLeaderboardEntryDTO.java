package com.musiguessr.backend.dto.tournament;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentLeaderboardEntryDTO {
    private Integer rank;
    private Long userId;
    private String username;
    private Integer score;
}
