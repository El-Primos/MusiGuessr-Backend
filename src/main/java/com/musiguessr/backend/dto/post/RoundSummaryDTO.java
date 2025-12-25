package com.musiguessr.backend.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoundSummaryDTO {
    private boolean response;
    private String songName;
    private String guessedSong;
    private Long guessTime;
    private Integer scoreEarned;
    private Integer round;
}
