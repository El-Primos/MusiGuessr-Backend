package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.GameHistory;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {

    List<GameHistory> findByGameId(Long gameId);

    List<GameHistory> findByUserId(Long userId);

    boolean existsByGameIdAndUserId(Long gameId, Long userId);

    Optional<GameHistory> findByGameIdAndUserId(Long gameId, Long userId);

    // Global leaderboard - MAX score per user (all time)
    @Query("""
        SELECT gh.userId, MAX(gh.score) as maxScore
        FROM GameHistory gh
        GROUP BY gh.userId
        ORDER BY maxScore DESC
        """)
    List<Object[]> findGlobalLeaderboard(Pageable pageable);

    // Global leaderboard with date filter - MAX score per user
    @Query("""
        SELECT gh.userId, MAX(gh.score) as maxScore
        FROM GameHistory gh
        JOIN gh.game g
        WHERE g.createdAt >= :since
        GROUP BY gh.userId
        ORDER BY maxScore DESC
        """)
    List<Object[]> findGlobalLeaderboardSince(@Param("since") OffsetDateTime since, Pageable pageable);

    // Playlist leaderboard - MAX score per user for specific playlist
    @Query("""
        SELECT gh.userId, MAX(gh.score) as maxScore
        FROM GameHistory gh
        JOIN gh.game g
        WHERE g.playlistId = :playlistId
        GROUP BY gh.userId
        ORDER BY maxScore DESC
        """)
    List<Object[]> findPlaylistLeaderboard(@Param("playlistId") Long playlistId, Pageable pageable);
}

