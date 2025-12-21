package com.musiguessr.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TournamentParticipantId implements Serializable {

    @Column(name = "tournament_id")
    private Long tournamentId;

    @Column(name = "user_id")
    private Long userId;
}
