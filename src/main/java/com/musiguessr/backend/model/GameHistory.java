package com.musiguessr.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_history", schema = "musiguessr_schema")
public class GameHistory {

    @EmbeddedId
    private GameHistoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("gameId")
    @JoinColumn(
            name = "game_id",
            foreignKey = @ForeignKey(name = "game_history_game_id_fkey")
    )
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "game_history_user_id_fkey")
    )
    private User user;

    @Column(name = "user_score", nullable = false)
    private Integer userScore = 0;
}
