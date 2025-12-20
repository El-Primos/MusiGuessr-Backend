package com.musiguessr.backend.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "games", schema = "musiguessr_schema")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "games_id_seq_gen")
    @SequenceGenerator(
            name = "games_id_seq_gen",
            sequenceName = "musiguessr_schema.games_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "creator_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "games_creator_id_fkey")
    )
    private User creator;

    @Column(name = "is_offline", nullable = false)
    private Boolean isOffline = false;

    @Column
    private String type;

    @Column(name = "played_at", nullable = false, insertable = false)
    private OffsetDateTime playedAt;

    @Column(name = "playlist_id")
    private Long playlistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "playlist_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "games_playlist_id_fkey")
    )
    private Playlist playlist;
}
