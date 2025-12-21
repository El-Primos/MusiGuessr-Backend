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
@Table(name = "tournaments", schema = "musiguessr_schema")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tournaments_id_seq_gen")
    @SequenceGenerator(
            name = "tournaments_id_seq_gen",
            sequenceName = "musiguessr_schema.tournaments_id_seq",
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
            foreignKey = @ForeignKey(name = "tournaments_creator_id_fkey")
    )
    private User creator;

    @Column(name = "playlist_id")
    private Long playlistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "playlist_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "tournaments_playlist_id_fkey")
    )
    private Playlist playlist;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TournamentStatus status = TournamentStatus.UPCOMING;

    @Column(name = "create_date", insertable = false, updatable = false)
    private OffsetDateTime createDate;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;
}
