package com.musiguessr.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_rounds")
public class GameRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_history_id", nullable = false)
    private Long gameHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_history_id", insertable = false, updatable = false)
    private GameHistory gameHistory;

    @Column(name = "song", nullable = false)
    private String song;

    @Column(name = "guessed_song")
    private String guessedSong;

    @Column(name = "guess_time", nullable = false)
    private Long guessTime;

    @Column(nullable = false)
    private Boolean guessed = false;

    @Column(name = "score_earned", nullable = false)
    private Integer scoreEarned;

    @Column(name = "round", nullable = false)
    private Integer round;
}
