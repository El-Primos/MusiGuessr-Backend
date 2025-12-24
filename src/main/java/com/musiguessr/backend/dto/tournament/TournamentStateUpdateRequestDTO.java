package com.musiguessr.backend.dto.tournament;

import com.musiguessr.backend.model.TournamentState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentStateUpdateRequestDTO {
    private TournamentState state;
}

