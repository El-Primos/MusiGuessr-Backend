package com.musiguessr.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tournament_info", schema = "musiguessr_schema")
public class TournamentParticipant {

    @EmbeddedId
    private TournamentParticipantId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tournamentId")
    @JoinColumn(
            name = "tournament_id",
            foreignKey = @ForeignKey(name = "tournament_info_tournament_id_fkey")
    )
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "tournament_info_user_id_fkey")
    )
    private User user;

    @Column(name = "user_score", nullable = false)
    private Integer userScore = 0;
}
