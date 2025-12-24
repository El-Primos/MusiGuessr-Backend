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
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "game_history_id", nullable = false)
    private Long gameHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_history_id", insertable = false, updatable = false)
    private GameHistory gameHistory;

    @Column(name = "posted_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime postedAt;
}
