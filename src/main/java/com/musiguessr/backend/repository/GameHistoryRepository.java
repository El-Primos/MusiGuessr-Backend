package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.GameHistory;
import com.musiguessr.backend.model.GameHistoryId;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistory, GameHistoryId> {

    List<GameHistory> findByIdGameId(Long gameId);

    List<GameHistory> findByIdUserId(Long userId);

    // Global leaderboard - MAX score per user (all time)
    @Query("""
        SELECT gh.id.userId, MAX(gh.userScore) as maxScore
        FROM GameHistory gh
        GROUP BY gh.id.userId
        ORDER BY maxScore DESC
        """)
    List<Object[]> findGlobalLeaderboard(Pageable pageable);

    // Global leaderboard with date filter - MAX score per user
    @Query("""
        SELECT gh.id.userId, MAX(gh.userScore) as maxScore
        FROM GameHistory gh
        JOIN gh.game g
        WHERE g.playedAt >= :since
        GROUP BY gh.id.userId
        ORDER BY maxScore DESC
        """)
    List<Object[]> findGlobalLeaderboardSince(@Param("since") OffsetDateTime since, Pageable pageable);

    // Playlist leaderboard - MAX score per user for specific playlist
    @Query("""
        SELECT gh.id.userId, MAX(gh.userScore) as maxScore
        FROM GameHistory gh
        JOIN gh.game g
        WHERE g.playlistId = :playlistId
        GROUP BY gh.id.userId
        ORDER BY maxScore DESC
        """)
    List<Object[]> findPlaylistLeaderboard(@Param("playlistId") Long playlistId, Pageable pageable);
}

